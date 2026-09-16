package com.example.controller;

import com.example.dto.BatchSalaryDTO;
import com.example.dto.SalaryDTO;
import com.example.pojo.BatchResult;
import com.example.pojo.PageResult;
import com.example.pojo.Result;
import com.example.pojo.Salary;
import com.example.pojo.SalaryVO;
import com.example.service.SalaryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 薪资管理控制器
 * 提供薪资的分页查询、单条保存、批量保存、删除、全局重算、薪资预计算接口
 */
@Slf4j
@RestController
@RequestMapping("/api/salary")
public class SalaryController {

    @Resource
    private SalaryService salaryService;

    /**
     * 分页查询薪资列表，支持多条件筛选
     * @param year 年份，可选
     * @param month 月份，可选
     * @param dept 部门，可选
     * @param name 员工姓名，可选
     * @param empId 员工ID，可选
     * @param sortOrder 排序方式，默认DESC降序
     * @param pageNum 当前页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 分页结果对象，包含薪资数据列表、总条数、总页数
     */
    @GetMapping("/list")
    public Result<PageResult<SalaryVO>> list(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer empId,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("查询薪资列表 - year:{}, month:{}, dept:{}, name:{}, empId:{}, sortOrder:{}, pageNum:{}, pageSize:{}",
                year, month, dept, name, empId, sortOrder, pageNum, pageSize);
        // 【新增】排序参数白名单校验，防止SQL注入
        if (!"ASC".equalsIgnoreCase(sortOrder) && !"DESC".equalsIgnoreCase(sortOrder)) {
            sortOrder = "DESC";
        }
        PageResult<SalaryVO> result = salaryService.list(year, month, dept, name, empId, sortOrder, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 单条保存薪资记录
     * @param dto 薪资保存入参，@Valid开启参数校验
     * @return 操作响应结果
     */
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody SalaryDTO dto) {
        log.info("保存薪资 - empId:{}, month:{}", dto.getEmpId(), dto.getMonth());
        salaryService.save(dto);
        return Result.successMsg("保存成功");
    }

    /**
     * 批量保存薪资记录
     * @param dto 批量薪资入参，包含操作模式、年月、员工id集合
     * @return 批量执行结果：成功条数、跳过条数（已存在记录会跳过）
     */
    @PostMapping("/batchSave")
    public Result<BatchResult> batchSave(@RequestBody BatchSalaryDTO dto) {
        log.info("批量保存薪资 - mode:{}, month:{}, year:{}, empIds:{}",
                dto.getMode(), dto.getMonth(), dto.getYear(), dto.getEmpIds());
        BatchResult result = salaryService.batchSave(dto);
        String message = "批量录入完成，成功 " + result.getSuccessCount() + " 条";
        if (result.getSkipCount() > 0) {
            message += "，跳过 " + result.getSkipCount() + " 条（已有工资记录）";
        }
        return Result.success(message, result);
    }

    /**
     * 根据主键id删除薪资记录
     * @param id 薪资记录主键id
     * @return 操作响应结果
     */
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Integer id) {
        log.info("删除薪资 - id:{}", id);
        salaryService.delete(id);
        return Result.successMsg("删除成功");
    }

    /**
     * 全局批量核对并重算全部工资
     * 遍历全部薪资记录，按照业务规则重新计算薪资
     * @return 操作响应结果
     */
    @GetMapping("/checkAll")
    public Result<Void> checkAll() {
        log.info("批量核对重算所有工资");
        salaryService.checkAllSalary();
        return Result.successMsg("重算完成");
    }

    /**
     * 薪资预计算接口，仅计算，**不会存入数据库**，用于前端预览薪资
     * @param empId 员工ID
     * @param month 要计算的月份
     * @return 计算完成后的薪资实体对象
     */
    @GetMapping("/calculate")
    public Result<Salary> calculate(@RequestParam Integer empId, @RequestParam String month) {
        log.info("预计算薪资 - empId:{}, month:{}", empId, month);
        Salary salary = salaryService.calculateSalaryByEmpAndMonth(empId, month);
        return Result.success(salary);
    }
}