package com.example.service;

import com.example.dto.ChangePasswordDTO;
import com.example.dto.EmployeeDTO;
import com.example.exception.BusinessException;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.EmployeeMapper;
import com.example.pojo.Employee;
import com.example.pojo.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;

    public PageResult<Employee> list(Integer deptId, Integer postId, String name, Integer pageNum, Integer pageSize) {
        log.debug("查询员工列表 - deptId:{}, postId:{}, name:{}", deptId, postId, name);
        PageHelper.startPage(pageNum, pageSize);
        Page<Employee> page = (Page<Employee>) employeeMapper.list(deptId, postId, name);
        return PageResult.of(page.getResult(), page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public void add(EmployeeDTO dto) {
        log.debug("新增员工 - {}", dto.getName());
        Employee employee = new Employee();
        copyDtoToEntity(dto, employee);
        employeeMapper.insert(employee);
        log.info("员工新增成功 - id:{}, name:{}", employee.getId(), employee.getName());
    }

    @Transactional
    public void update(EmployeeDTO dto) {
        log.debug("更新员工 - id:{}, name:{}", dto.getId(), dto.getName());
        Employee employee = employeeMapper.findById(dto.getId());
        if (employee == null) {
            throw new ResourceNotFoundException("员工不存在");
        }
        copyDtoToEntity(dto, employee);
        employeeMapper.update(employee);
        log.info("员工更新成功 - id:{}, name:{}", employee.getId(), employee.getName());
    }

    @Transactional
    public void delete(Integer id) {
        log.debug("删除员工 - id:{}", id);
        Employee employee = employeeMapper.findById(id);
        if (employee == null) {
            throw new ResourceNotFoundException("员工不存在");
        }

        // 检查关联数据
        StringBuilder errorMsg = new StringBuilder();
        if (employeeMapper.countAttendanceByEmpId(id) > 0) {
            errorMsg.append("考勤记录、");
        }
        if (employeeMapper.countSalaryByEmpId(id) > 0) {
            errorMsg.append("薪资记录、");
        }
        if (employeeMapper.countYearEndBonusByEmpId(id) > 0) {
            errorMsg.append("年终奖记录、");
        }

        if (errorMsg.length() > 0) {
            errorMsg.deleteCharAt(errorMsg.length() - 1);
            throw new BusinessException("该员工存在" + errorMsg + "，无法删除");
        }

        employeeMapper.delete(id);
        log.info("员工删除成功 - id:{}, name:{}", id, employee.getName());
    }

    public Employee getById(Integer id) {
        log.debug("查询员工 - id:{}", id);
        Employee employee = employeeMapper.findById(id);
        if (employee == null) {
            throw new ResourceNotFoundException("员工不存在");
        }
        return employee;
    }

    private void copyDtoToEntity(EmployeeDTO dto, Employee employee) {
        employee.setId(dto.getId());
        employee.setName(dto.getName());
        employee.setDeptId(dto.getDeptId());
        employee.setPostId(dto.getPostId());
        employee.setLevelId(dto.getLevelId());
        employee.setRoleId(dto.getRoleId());
        employee.setPersonalSalary(dto.getPersonalSalary());
        employee.setUsername(dto.getUsername());
        employee.setPassword(dto.getPassword());
    }

    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        log.debug("修改密码 - id:{}", dto.getId());
        Employee employee = employeeMapper.findById(dto.getId());
        if (employee == null) {
            throw new ResourceNotFoundException("员工不存在");
        }
        
        if (!dto.getOldPassword().equals(employee.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        
        employee.setPassword(dto.getNewPassword());
        employeeMapper.update(employee);
        log.info("密码修改成功 - id:{}", dto.getId());
    }

    @Transactional
    public void updatePersonalSalary(Integer empId, BigDecimal personalSalary) {
        log.debug("更新员工个人基本工资 - empId:{}, personalSalary:{}", empId, personalSalary);
        Employee employee = employeeMapper.findById(empId);
        if (employee == null) {
            throw new ResourceNotFoundException("员工不存在");
        }
        employee.setPersonalSalary(personalSalary);
        employeeMapper.update(employee);
        log.info("个人基本工资更新成功 - empId:{}, personalSalary:{}", empId, personalSalary);
    }
}