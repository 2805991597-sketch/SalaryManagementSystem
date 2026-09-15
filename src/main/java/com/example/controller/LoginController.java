package com.example.controller;

import com.example.dto.LoginDTO;
import com.example.pojo.Employee;
import com.example.pojo.Result;
import com.example.service.LoginService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class LoginController {

    @Resource
    private LoginService loginService;

    @PostMapping("/login")
    public Result<Employee> login(@Valid @RequestBody LoginDTO dto) {
        log.info("用户登录 - username:{}", dto.getUsername());
        Employee employee = loginService.login(dto.getUsername(), dto.getPassword());
        return Result.success(employee);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户退出登录");
        return Result.successMsg("退出成功");
    }
}
