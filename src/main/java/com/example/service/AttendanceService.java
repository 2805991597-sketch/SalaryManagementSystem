package com.example.service;

import com.example.dto.AttendanceDTO;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.AttendanceMapper;
import com.example.mapper.EmployeeMapper;
import com.example.pojo.Attendance;
import com.example.pojo.Employee;
import com.example.pojo.PageResult;
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

@Slf4j
@Service
public class AttendanceService {

    @Resource
    private AttendanceMapper attendanceMapper;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private SalaryService salaryService;

    private static final BigDecimal LATE_FEE_PER_TIME = new BigDecimal("20");
    private static final BigDecimal ABSENT_FEE_PER_DAY = new BigDecimal("100");

    public PageResult<Attendance> list(String year, String month, String dept, String name, Integer empId, String sortOrder, Integer pageNum, Integer pageSize) {
        log.debug("查询考勤列表 - year:{}, month:{}, dept:{}, name:{}, empId:{}, sortOrder:{}", year, month, dept, name, empId, sortOrder);
        PageHelper.startPage(pageNum, pageSize);
        Page<Attendance> page = (Page<Attendance>) attendanceMapper.list(year, month, dept, name, empId, sortOrder);
        return PageResult.of(page.getResult(), page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public void save(AttendanceDTO dto) {
        log.info("保存考勤 - id:{}, empId:{}, month:{}, absentDays:{}, lateTimes:{}, fine:{}", 
                dto.getId(), dto.getEmpId(), dto.getMonth(), dto.getAbsentDays(), dto.getLateTimes(), dto.getFine());
        
        Employee emp = employeeMapper.findById(dto.getEmpId());
        if (emp == null) {
            throw new ResourceNotFoundException("员工不存在");
        }

        Attendance attendance = new Attendance();
        attendance.setId(dto.getId());
        attendance.setEmpId(dto.getEmpId());
        LocalDate month = parseMonth(dto.getMonth());
        attendance.setMonth(month);
        attendance.setAbsentDays(dto.getAbsentDays() == null ? 0 : dto.getAbsentDays());
        attendance.setLateTimes(dto.getLateTimes() == null ? 0 : dto.getLateTimes());

        if (dto.getFine() != null) {
            attendance.setFine(dto.getFine());
            log.info("使用前端传入的fine值: {}", dto.getFine());
        } else {
            calculateFine(attendance);
            log.info("自动计算fine值: {}", attendance.getFine());
        }

        log.info("准备保存的考勤数据: id={}, empId={}, month={}, absentDays={}, lateTimes={}, fine={}", 
                attendance.getId(), attendance.getEmpId(), attendance.getMonth(), 
                attendance.getAbsentDays(), attendance.getLateTimes(), attendance.getFine());

        if (attendance.getId() == null) {
            int result = attendanceMapper.insert(attendance);
            log.info("考勤新增成功 - 影响行数:{}, empId:{}, month:{}", result, dto.getEmpId(), dto.getMonth());
        } else {
            int result = attendanceMapper.update(attendance);
            log.info("考勤更新成功 - 影响行数:{}, id:{}, empId:{}, month:{}", result, attendance.getId(), dto.getEmpId(), dto.getMonth());
        }

        // 工资一旦录入后保持不变，不再根据考勤自动更新
        // salaryService.updateSalaryByAttendance(dto.getEmpId(), month);
    }

    private void calculateFine(Attendance attendance) {
        BigDecimal lateFine = BigDecimal.valueOf(attendance.getLateTimes()).multiply(LATE_FEE_PER_TIME);
        BigDecimal absentFine = BigDecimal.valueOf(attendance.getAbsentDays()).multiply(ABSENT_FEE_PER_DAY);
        attendance.setFine(lateFine.add(absentFine));
    }

    private LocalDate parseMonth(String monthStr) {
        if (monthStr == null) {
            return null;
        }
        try {
            if (monthStr.length() == 7) {
                YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
                return yearMonth.atDay(1);
            } else if (monthStr.length() == 10) {
                return LocalDate.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                return LocalDate.parse(monthStr);
            }
        } catch (Exception e) {
            log.error("日期解析失败: {}", monthStr, e);
            return null;
        }
    }

    @Transactional
    public void delete(Integer id) {
        log.debug("删除考勤 - id:{}", id);
        Attendance attendance = attendanceMapper.findById(id);
        if (attendance == null) {
            throw new ResourceNotFoundException("考勤记录不存在");
        }
        attendanceMapper.delete(id);
        log.info("考勤删除成功 - id:{}", id);
    }
}