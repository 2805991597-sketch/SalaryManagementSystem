package com.example.pojo;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class Attendance {
    private Integer id;
    private Integer empId;            // 员工ID
    private LocalDate month;         // 考勤月份（按月）
    private Integer absentDays;  // 缺勤天数
    private Integer lateTimes;  // 迟到次数
    private BigDecimal fine;    // 总罚金（自动计算）
    // 联表展示字段
    private String name;             // 员工姓名
    private String deptName;         // 部门名称
}