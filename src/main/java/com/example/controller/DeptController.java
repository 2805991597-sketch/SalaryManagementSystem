package com.example.controller;

import com.example.pojo.Dept;
import com.example.pojo.Result;
import com.example.service.DeptService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/dept")
public class DeptController {
    @Resource
    private DeptService deptService;

    @GetMapping("/list")
    public Result<List<Dept>> list(@RequestParam(required = false) String deptName) {
        log.info("查询部门列表 - deptName:{}", deptName);
        List<Dept> list = deptService.search(deptName);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Dept dept) {
        log.info("新增部门 - {}", dept.getDeptName());
        deptService.add(dept);
        return Result.successMsg("添加成功");
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody Dept dept) {
        log.info("更新部门 - {}", dept.getDeptName());
        deptService.update(dept);
        return Result.successMsg("修改成功");
    }

    @GetMapping("/delete")
    public Result<Void> delete(Integer id) {
        log.info("删除部门 - id:{}", id);
        try {
            deptService.delete(id);
            return Result.successMsg("删除成功");
        } catch (RuntimeException e) {
            log.warn("删除部门失败 - {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}