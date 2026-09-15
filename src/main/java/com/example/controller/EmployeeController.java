package com.example.controller;

import com.example.dto.ChangePasswordDTO;
import com.example.dto.EmployeeDTO;
import com.example.pojo.Employee;
import com.example.pojo.PageResult;
import com.example.pojo.Result;
import com.example.service.EmployeeService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @GetMapping("/list")
    public Result<PageResult<Employee>> list(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Integer postId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("查询员工列表 - deptId:{}, postId:{}, name:{}, pageNum:{}, pageSize:{}", 
                deptId, postId, name, pageNum, pageSize);
        PageResult<Employee> result = employeeService.list(deptId, postId, name, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody EmployeeDTO dto) {
        log.info("新增员工 - {}", dto.getName());
        employeeService.add(dto);
        return Result.successMsg("新增成功");
    }

    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody EmployeeDTO dto) {
        log.info("更新员工 - {}", dto.getName());
        employeeService.update(dto);
        return Result.successMsg("更新成功");
    }

    @GetMapping("/delete")
    public Result<Void> delete(@RequestParam Integer id) {
        log.info("删除员工 - id:{}", id);
        employeeService.delete(id);
        return Result.successMsg("删除成功");
    }

    @GetMapping("/get")
    public Result<Employee> get(@RequestParam Integer id) {
        log.info("查询员工 - id:{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        log.info("修改密码 - id:{}", dto.getId());
        employeeService.changePassword(dto);
        return Result.successMsg("密码修改成功");
    }

    @PostMapping("/updatePersonalSalary")
    public Result<Void> updatePersonalSalary(@RequestBody EmployeeDTO dto) {
        log.info("更新员工个人基本工资 - empId:{}, personalSalary:{}", dto.getId(), dto.getPersonalSalary());
        employeeService.updatePersonalSalary(dto.getId(), dto.getPersonalSalary());
        return Result.successMsg("个人基本工资更新成功");
    }
}