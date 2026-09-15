package com.example.controller;

import com.example.dto.AttendanceDTO;
import com.example.pojo.Attendance;
import com.example.pojo.PageResult;
import com.example.pojo.Result;
import com.example.service.AttendanceService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Resource
    private AttendanceService attendanceService;

    @GetMapping("/list")
    public Result<PageResult<Attendance>> list(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer empId,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("查询考勤列表 - year:{}, month:{}, dept:{}, name:{}, empId:{}, sortOrder:{}, pageNum:{}, pageSize:{}",
                year, month, dept, name, empId, sortOrder, pageNum, pageSize);
        PageResult<Attendance> result = attendanceService.list(year, month, dept, name, empId, sortOrder, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody AttendanceDTO dto) {
        log.info("收到考勤保存请求 - id:{}, empId:{}, month:{}, absentDays:{}, lateTimes:{}, fine:{}", 
                dto.getId(), dto.getEmpId(), dto.getMonth(), dto.getAbsentDays(), dto.getLateTimes(), dto.getFine());
        try {
            attendanceService.save(dto);
            log.info("考勤保存成功");
            return Result.successMsg("保存成功");
        } catch (Exception e) {
            log.error("考勤保存失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    @GetMapping("/delete")
    public Result<Void> delete(@RequestParam Integer id) {
        log.info("删除考勤 - id:{}", id);
        attendanceService.delete(id);
        return Result.successMsg("删除成功");
    }
}