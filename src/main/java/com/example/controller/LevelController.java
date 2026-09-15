package com.example.controller;

import com.example.pojo.Level;
import com.example.pojo.Result;
import com.example.service.LevelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/level")
public class LevelController {

    @Resource
    private LevelService levelService;

    @GetMapping("/list")
    public Result<List<Level>> list() {
        log.info("查询等级列表");
        List<Level> list = levelService.list();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Level level) {
        log.info("新增等级 - {}", level.getLevelName());
        levelService.add(level);
        return Result.successMsg("添加成功");
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody Level level) {
        log.info("更新等级 - {}", level.getLevelName());
        levelService.update(level);
        return Result.successMsg("修改成功");
    }

    @PostMapping("/delete")
    public Result<Void> delete(Integer id) {
        log.info("删除等级 - id:{}", id);
        levelService.delete(id);
        return Result.successMsg("删除成功");
    }
}