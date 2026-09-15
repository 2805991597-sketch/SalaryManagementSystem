package com.example.mapper;

import com.example.pojo.Salary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SalaryMapper {

    /**
     * 多条件薪资列表查询
     */
    List<Salary> list(
            @Param("year") String year,
            @Param("month") String month,
            @Param("dept") String dept,
            @Param("name") String name,
            @Param("empId") Integer empId,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 新增薪资记录，useGeneratedKeys回填id
     */
    int insert(Salary salary);

    /**
     * 更新薪资记录
     */
    int update(Salary salary);

    /**
     * 根据id删除，返回受影响行数
     */
    int delete(Integer id);

    /**
     * 根据id查询薪资（联表带出姓名、部门）
     */
    Salary findById(Integer id);

    /**
     * 根据员工id+月份查询薪资
     * @param empId 员工id
     * @param month 月份 LocalDate，例如 2026‑09‑01
     */
    Salary findByEmpAndMonth(@Param("empId") Integer empId, @Param("month") LocalDate month);
}