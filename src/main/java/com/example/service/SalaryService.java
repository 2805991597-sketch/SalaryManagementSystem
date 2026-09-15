package com.example.service;

import com.example.dto.BatchSalaryDTO;
import com.example.dto.SalaryDTO;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.AttendanceMapper;
import com.example.mapper.PostMapper;
import com.example.mapper.SalaryMapper;
import com.example.mapper.SubsidyMapper;
import com.example.mapper.EmployeeMapper;
import com.example.mapper.LevelMapper;
import com.example.mapper.TaxDetailMapper;
import com.example.pojo.Attendance;
import com.example.pojo.BatchResult;
import com.example.pojo.Employee;
import com.example.pojo.PageResult;
import com.example.pojo.Post;
import com.example.pojo.Salary;
import com.example.pojo.Subsidy;
import com.example.pojo.TaxDetail;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SalaryService {

    @Resource
    private SalaryMapper salaryMapper;
    @Resource
    private SubsidyMapper subsidyMapper;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private AttendanceMapper attendanceMapper;
    @Resource
    private LevelMapper levelMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private TaxDetailMapper taxDetailMapper;

    public PageResult<Salary> list(String year, String month, String dept, String name, Integer empId, String sortOrder, Integer pageNum, Integer pageSize) {
        log.debug("查询薪资列表 - year:{}, month:{}, dept:{}, name:{}, empId:{}, sortOrder:{}", year, month, dept, name, empId, sortOrder);
        PageHelper.startPage(pageNum, pageSize);
        Page<Salary> page = (Page<Salary>) salaryMapper.list(year, month, dept, name, empId, sortOrder);
        return PageResult.of(page.getResult(), page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public void save(SalaryDTO dto) {
        log.debug("保存薪资 - empId:{}, month:{}", dto.getEmpId(), dto.getMonth());

        Employee emp = employeeMapper.findById(dto.getEmpId());
        if (emp == null) {
            throw new ResourceNotFoundException("员工不存在");
        }

        if (dto.getId() == null) {
            Salary existing = salaryMapper.findByEmpAndMonth(dto.getEmpId(), parseMonth(dto.getMonth()));
            if (existing != null) {
                throw new IllegalArgumentException(emp.getName() + " 在 " + dto.getMonth() + " 月份已有工资记录");
            }
        }

        Salary salary = new Salary();
        salary.setId(dto.getId());
        salary.setEmpId(dto.getEmpId());
        salary.setMonth(parseMonth(dto.getMonth()));
        // name仅内存赋值，数据库表无此字段，insert/update xml不会写入
        salary.setName(emp.getName());

        // 优先使用DTO传来的值，如果没有再从数据库查询
        if (dto.getBasicSalary() != null) {
            salary.setBasicSalary(dto.getBasicSalary());
        } else {
            // 首先检查员工的个人基本工资
            if (emp.getPersonalSalary() != null) {
                salary.setBasicSalary(emp.getPersonalSalary());
            } else {
                // 如果没有个人基本工资，使用职位的基本工资
                Post post = postMapper.getById(emp.getPostId());
                salary.setBasicSalary(post != null && post.getBaseSalary() != null
                        ? BigDecimal.valueOf(post.getBaseSalary())
                        : BigDecimal.ZERO);
            }
        }

        if (dto.getLevelSubsidy() != null) {
            salary.setLevelSubsidy(dto.getLevelSubsidy());
        } else {
            List<com.example.pojo.Level> levelList = levelMapper.list();
            com.example.pojo.Level level = levelList.stream()
                    .filter(l -> l.getId().equals(emp.getLevelId()))
                    .findFirst()
                    .orElse(null);
            salary.setLevelSubsidy(level != null ? level.getLevelSubsidy() : BigDecimal.ZERO);
        }

        if (dto.getFoodSubsidy() != null && dto.getTrafficSubsidy() != null && dto.getHousingSubsidy() != null) {
            salary.setFoodSubsidy(dto.getFoodSubsidy());
            salary.setTrafficSubsidy(dto.getTrafficSubsidy());
            salary.setHousingSubsidy(dto.getHousingSubsidy());
        } else {
            List<Subsidy> subsidyList = subsidyMapper.findAll();
            Subsidy subsidy = null;
            if (emp.getDeptId() != null) {
                subsidy = subsidyList.stream()
                        .filter(s -> s.getDeptId().equals(emp.getDeptId()))
                        .findFirst()
                        .orElse(null);
            }
            if (subsidy != null) {
                salary.setFoodSubsidy(subsidy.getFoodSubsidy());
                salary.setTrafficSubsidy(subsidy.getTrafficSubsidy());
                salary.setHousingSubsidy(subsidy.getHousingSubsidy());
            } else {
                // 如果没有匹配到补贴，设置默认值为0
                salary.setFoodSubsidy(BigDecimal.ZERO);
                salary.setTrafficSubsidy(BigDecimal.ZERO);
                salary.setHousingSubsidy(BigDecimal.ZERO);
                log.warn("未找到员工 {} (部门ID:{}) 的补贴信息，使用默认值", emp.getName(), emp.getDeptId());
            }
        }

        if (dto.getLateFine() != null) {
            salary.setLateFine(dto.getLateFine());
        } else {
            Attendance attendance = attendanceMapper.selectByEmpAndMonth(dto.getEmpId(), parseMonth(dto.getMonth()));
            if (attendance != null) {
                salary.setLateFine(attendance.getFine());
            } else {
                salary.setLateFine(BigDecimal.ZERO);
            }
        }

        calculateSalary(salary);

        if (salary.getId() == null) {
            salaryMapper.insert(salary);
            log.info("薪资新增成功 - empId:{}, month:{}", dto.getEmpId(), dto.getMonth());
        } else {
            salaryMapper.update(salary);
            log.info("薪资更新成功 - id:{}, empId:{}, month:{}", salary.getId(), dto.getEmpId(), dto.getMonth());
        }
    }

    private void calculateSalary(Salary salary) {
        BigDecimal basic = orZero(salary.getBasicSalary());
        BigDecimal food = orZero(salary.getFoodSubsidy());
        BigDecimal traffic = orZero(salary.getTrafficSubsidy());
        BigDecimal housing = orZero(salary.getHousingSubsidy());
        BigDecimal level = orZero(salary.getLevelSubsidy());
        BigDecimal lateFine = orZero(salary.getLateFine());

        BigDecimal shouldPay = basic.add(food).add(traffic).add(housing).add(level);

        BigDecimal taxableIncome = shouldPay.subtract(lateFine).subtract(new BigDecimal("5000"));
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }

        BigDecimal tax = calculateTax(taxableIncome);
        salary.setTax(tax);

        BigDecimal actual = shouldPay.subtract(lateFine).subtract(tax);
        salary.setActualSalary(actual);
    }

    private BigDecimal orZero(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

    private BigDecimal calculateTax(BigDecimal income) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        List<TaxDetail> taxRates = taxDetailMapper.list("个人所得税", true);
        for (TaxDetail rate : taxRates) {
            BigDecimal min = rate.getMinThreshold() != null ? rate.getMinThreshold() : BigDecimal.ZERO;
            BigDecimal max = rate.getMaxLimit();
            BigDecimal taxRate = rate.getRate();
            BigDecimal quickDeduction = rate.getQuickDeduction() != null ? rate.getQuickDeduction() : BigDecimal.ZERO;

            if (max != null) {
                if (income.compareTo(min) > 0 && income.compareTo(max) <= 0) {
                    return income.multiply(taxRate).subtract(quickDeduction).setScale(2, java.math.RoundingMode.HALF_UP);
                }
            } else {
                if (income.compareTo(min) > 0) {
                    return income.multiply(taxRate).subtract(quickDeduction).setScale(2, java.math.RoundingMode.HALF_UP);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private LocalDate parseMonth(String monthStr) {
        YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        return yearMonth.atDay(1);
    }

    @Transactional
    public void delete(Integer id) {
        log.debug("删除薪资 - id:{}", id);
        Salary salary = salaryMapper.findById(id);
        if (salary == null) {
            throw new ResourceNotFoundException("薪资记录不存在");
        }
        salaryMapper.delete(id);
        log.info("薪资删除成功 - id:{}", id);
    }

    @Transactional
    public void checkAllSalary() {
        log.warn("批量核对重算工资功能已禁用 - 为了保持历史数据的完整性，不再支持批量重算");
        // 注释掉下面的代码，避免修改历史工资数据
        /*
        log.info("开始批量核对重算所有工资");
        List<Salary> allSalaryList = salaryMapper.list(null, null, null, null);
        log.info("待重算薪资记录数: {}", allSalaryList.size());

        List<Subsidy> subsidyList = subsidyMapper.findAll();
        List<com.example.pojo.Level> levelList = levelMapper.list();

        for (Salary salary : allSalaryList) {
            Employee emp = employeeMapper.findById(salary.getEmpId());
            if (emp == null) continue;

            Subsidy subsidy = subsidyList.stream()
                    .filter(s -> s.getDeptId().equals(emp.getDeptId()))
                    .findFirst()
                    .orElse(null);

            if (subsidy != null) {
                salary.setFoodSubsidy(subsidy.getFoodSubsidy());
                salary.setTrafficSubsidy(subsidy.getTrafficSubsidy());
                salary.setHousingSubsidy(subsidy.getHousingSubsidy());
                salary.setDeptName(subsidy.getDeptName());
            }

            com.example.pojo.Level level = levelList.stream()
                    .filter(l -> l.getId().equals(emp.getLevelId()))
                    .findFirst()
                    .orElse(null);

            if (level != null) {
                salary.setLevelSubsidy(level.getLevelSubsidy());
            }

            Attendance attendance = attendanceMapper.selectByEmpAndMonth(salary.getEmpId(), salary.getMonth());
            if (attendance != null) {
                salary.setLateFine(attendance.getFine());
            } else {
                salary.setLateFine(BigDecimal.ZERO);
            }

            calculateSalary(salary);
            salaryMapper.update(salary);
        }

        log.info("批量重算完成");
        */
    }

    @Transactional
    public BatchResult batchSave(BatchSalaryDTO dto) {
        log.info("开始批量保存薪资 - mode:{}, month:{}, year:{}, empIds:{}",
                dto.getMode(), dto.getMonth(), dto.getYear(), dto.getEmpIds());

        List<String> months = new ArrayList<>();
        if ("single".equals(dto.getMode())) {
            months.add(dto.getMonth());
        } else if ("year".equals(dto.getMode())) {
            for (int m = 1; m <= 12; m++) {
                months.add(String.format("%d-%02d", dto.getYear(), m));
            }
        }

        List<Subsidy> subsidyList = subsidyMapper.findAll();
        List<com.example.pojo.Level> levelList = levelMapper.list();
        // ⚠️ 注意：PostMapper必须要有list(null,null)方法，否则会报错
        List<Post> postList = postMapper.list(null, null);

        BatchResult result = new BatchResult();

        for (String month : months) {
            for (Integer empId : dto.getEmpIds()) {
                Employee emp = employeeMapper.findById(empId);
                if (emp == null) continue;

                Salary existing = salaryMapper.findByEmpAndMonth(empId, parseMonth(month));
                if (existing != null) {
                    log.info("跳过已存在的薪资记录 - empId:{}, month:{}", empId, month);
                    result.getSkipMessages().add(emp.getName() + " 在 " + month + " 月份已有工资记录");
                    result.setSkipCount(result.getSkipCount() + 1);
                    continue;
                }

                Salary salary = new Salary();
                salary.setEmpId(empId);
                salary.setMonth(parseMonth(month));
                salary.setName(emp.getName());

                // 优先使用员工的个人基本工资，否则使用职位基本工资
                if (emp.getPersonalSalary() != null) {
                    salary.setBasicSalary(emp.getPersonalSalary());
                } else {
                    Post post = postList.stream()
                            .filter(p -> p.getId().equals(emp.getPostId()))
                            .findFirst()
                            .orElse(null);
                    if (post != null && post.getBaseSalary() != null) {
                        salary.setBasicSalary(BigDecimal.valueOf(post.getBaseSalary()));
                    }
                }

                levelList.stream()
                        .filter(l -> l.getId().equals(emp.getLevelId()))
                        .findFirst().ifPresent(level -> salary.setLevelSubsidy(level.getLevelSubsidy()));

                Subsidy subsidy = null;
                if (emp.getDeptId() != null) {
                    subsidy = subsidyList.stream()
                            .filter(s -> s.getDeptId().equals(emp.getDeptId()))
                            .findFirst()
                            .orElse(null);
                }
                if (subsidy != null) {
                    salary.setFoodSubsidy(subsidy.getFoodSubsidy());
                    salary.setTrafficSubsidy(subsidy.getTrafficSubsidy());
                    salary.setHousingSubsidy(subsidy.getHousingSubsidy());
                    // deptName仅内存赋值，数据库无此字段
                    salary.setDeptName(subsidy.getDeptName());
                } else {
                    salary.setFoodSubsidy(BigDecimal.ZERO);
                    salary.setTrafficSubsidy(BigDecimal.ZERO);
                    salary.setHousingSubsidy(BigDecimal.ZERO);
                    log.warn("未找到员工 {} (部门ID:{}) 的补贴信息，使用默认值", emp.getName(), emp.getDeptId());
                }

                Attendance attendance = attendanceMapper.selectByEmpAndMonth(empId, parseMonth(month));
                if (attendance != null) {
                    salary.setLateFine(attendance.getFine());
                } else {
                    salary.setLateFine(BigDecimal.ZERO);
                }

                calculateSalary(salary);
                salaryMapper.insert(salary);
                result.setSuccessCount(result.getSuccessCount() + 1);
                log.info("薪资新增成功 - empId:{}, month:{}", empId, month);
            }
        }

        log.info("批量保存完成，成功 {} 条，跳过 {} 条", result.getSuccessCount(), result.getSkipCount());
        return result;
    }

    @Transactional
    public void updateSalaryByAttendance(Integer empId, LocalDate month) {
        log.info("根据考勤更新工资 - empId:{}, month:{}", empId, month);

        Salary salary = salaryMapper.findByEmpAndMonth(empId, month);
        if (salary == null) {
            log.info("未找到对应月份的工资记录，无需更新 - empId:{}, month:{}", empId, month);
            return;
        }

        Attendance attendance = attendanceMapper.selectByEmpAndMonth(empId, month);
        BigDecimal oldLateFine = salary.getLateFine();

        if (attendance != null) {
            salary.setLateFine(attendance.getFine());
        } else {
            salary.setLateFine(BigDecimal.ZERO);
        }

        // 只更新考勤罚款，保持基本工资、补贴、个税等其他字段不变
        // 重新计算实发工资（应发工资 - 考勤罚款 - 个税），但保持个税不变
        BigDecimal totalIncome = orZero(salary.getBasicSalary())
                .add(orZero(salary.getFoodSubsidy()))
                .add(orZero(salary.getTrafficSubsidy()))
                .add(orZero(salary.getHousingSubsidy()))
                .add(orZero(salary.getLevelSubsidy()));

        BigDecimal actualSalary = totalIncome.subtract(orZero(salary.getLateFine())).subtract(orZero(salary.getTax()));
        salary.setActualSalary(actualSalary);

        salaryMapper.update(salary);
        log.info("工资更新成功 - empId:{}, month:{}, oldLateFine:{}, newLateFine:{}", empId, month, oldLateFine, salary.getLateFine());
    }

    public Salary calculateSalaryByEmpAndMonth(Integer empId, String monthStr) {
        log.debug("预计算薪资 - empId:{}, month:{}", empId, monthStr);

        Employee emp = employeeMapper.findById(empId);
        if (emp == null) {
            log.warn("员工不存在 - empId:{}", empId);
            return createEmptySalary();
        }

        LocalDate month = parseMonth(monthStr);

        Salary salary = new Salary();
        salary.setEmpId(empId);
        salary.setMonth(month);
        salary.setName(emp.getName());

        // 计算基本工资
        if (emp.getPersonalSalary() != null) {
            salary.setBasicSalary(emp.getPersonalSalary());
        } else {
            Post post = postMapper.getById(emp.getPostId());
            salary.setBasicSalary(post != null && post.getBaseSalary() != null
                    ? BigDecimal.valueOf(post.getBaseSalary())
                    : BigDecimal.ZERO);
        }

        // 计算职级补贴
        List<com.example.pojo.Level> levelList = levelMapper.list();
        com.example.pojo.Level level = levelList.stream()
                .filter(l -> l.getId().equals(emp.getLevelId()))
                .findFirst()
                .orElse(null);
        salary.setLevelSubsidy(level != null ? level.getLevelSubsidy() : BigDecimal.ZERO);

        // 计算部门补贴
        List<Subsidy> subsidyList = subsidyMapper.findAll();
        Subsidy subsidy = null;
        if (emp.getDeptId() != null) {
            subsidy = subsidyList.stream()
                    .filter(s -> s.getDeptId().equals(emp.getDeptId()))
                    .findFirst()
                    .orElse(null);
        }
        if (subsidy != null) {
            salary.setFoodSubsidy(subsidy.getFoodSubsidy());
            salary.setTrafficSubsidy(subsidy.getTrafficSubsidy());
            salary.setHousingSubsidy(subsidy.getHousingSubsidy());
        } else {
            salary.setFoodSubsidy(BigDecimal.ZERO);
            salary.setTrafficSubsidy(BigDecimal.ZERO);
            salary.setHousingSubsidy(BigDecimal.ZERO);
            log.warn("未找到员工 {} (部门ID:{}) 的补贴信息，使用默认值", emp.getName(), emp.getDeptId());
        }

        // 计算考勤罚金
        Attendance attendance = attendanceMapper.selectByEmpAndMonth(empId, month);
        if (attendance != null) {
            salary.setLateFine(attendance.getFine());
        } else {
            salary.setLateFine(BigDecimal.ZERO);
        }

        // 计算个税和实发工资
        calculateSalary(salary);

        log.info("预计算薪资完成 - empId:{}, basicSalary:{}, levelSubsidy:{}, foodSubsidy:{}, trafficSubsidy:{}, housingSubsidy:{}, lateFine:{}, tax:{}, actualSalary:{}",
                empId, salary.getBasicSalary(), salary.getLevelSubsidy(), salary.getFoodSubsidy(),
                salary.getTrafficSubsidy(), salary.getHousingSubsidy(), salary.getLateFine(),
                salary.getTax(), salary.getActualSalary());

        return salary;
    }

    private Salary createEmptySalary() {
        Salary salary = new Salary();
        salary.setBasicSalary(BigDecimal.ZERO);
        salary.setLevelSubsidy(BigDecimal.ZERO);
        salary.setFoodSubsidy(BigDecimal.ZERO);
        salary.setTrafficSubsidy(BigDecimal.ZERO);
        salary.setHousingSubsidy(BigDecimal.ZERO);
        salary.setLateFine(BigDecimal.ZERO);
        salary.setTax(BigDecimal.ZERO);
        salary.setActualSalary(BigDecimal.ZERO);
        return salary;
    }
}