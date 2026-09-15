package com.example.controller;

import com.example.pojo.Result;
import com.example.pojo.Subsidy;
import com.example.service.SubsidyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/subsidy")
public class SubsidyController {
    @Resource
    SubsidyService service;

    @GetMapping("/list")
    public Result<List<Subsidy>> list() {
        log.info("查询补贴列表");
        return Result.success(service.getAll());
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Subsidy s) {
        log.info("新增补贴 - deptId:{}", s.getDeptId());
        service.add(s);
        return Result.successMsg("添加成功");
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody Subsidy s) {
        log.info("更新补贴 - deptId:{}", s.getDeptId());
        service.update(s);
        return Result.successMsg("修改成功");
    }

    @GetMapping("/delete")
    public Result<Void> delete(Integer id) {
        log.info("删除补贴 - id:{}", id);
        service.delete(id);
        return Result.successMsg("删除成功");
    }
}