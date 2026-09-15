package com.example.controller;

import com.example.dto.RoleDTO;
import com.example.pojo.Result;
import com.example.pojo.Role;
import com.example.service.RoleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @GetMapping("/list")
    public Result<List<Role>> list() {
        log.info("查询角色列表");
        List<Role> result = roleService.list();
        return Result.success(result);
    }

    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody RoleDTO dto) {
        log.info("保存角色 - roleName:{}, roleCode:{}", dto.getRoleName(), dto.getRoleCode());
        roleService.save(dto);
        return Result.successMsg("保存成功");
    }

    @GetMapping("/delete")
    public Result<Void> delete(@RequestParam Integer id) {
        log.info("删除角色 - id:{}", id);
        roleService.delete(id);
        return Result.successMsg("删除成功");
    }
}
