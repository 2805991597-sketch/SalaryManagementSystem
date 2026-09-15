package com.example.service;

import com.example.exception.BusinessException;
import com.example.mapper.EmployeeMapper;
import com.example.pojo.Employee;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginService {

    @Resource
    private EmployeeMapper employeeMapper;

    public Employee login(String username, String password) {
        log.debug("登录验证 - username:{}", username);
        
        Employee employee = employeeMapper.findByUsername(username);
        if (employee == null) {
            throw new BusinessException("账号不存在");
        }
        
        if (!password.equals(employee.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        log.info("登录成功 - username:{}, roleId:{}", username, employee.getRoleId());
        return employee;
    }
}
