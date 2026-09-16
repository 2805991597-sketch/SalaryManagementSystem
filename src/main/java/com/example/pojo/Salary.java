package com.example.pojo;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Salary {
    private Integer id;
    private Integer empId;
    private LocalDate month;

    private BigDecimal basicSalary;       // 基本工资
    private BigDecimal foodSubsidy;       // 餐补
    private BigDecimal trafficSubsidy;    // 交通补
    private BigDecimal housingSubsidy;    // 住房补
    private BigDecimal levelSubsidy;      // 等级补贴
    private BigDecimal lateFine;          // 迟到罚金
    private BigDecimal tax;               // 个人所得税
    private BigDecimal actualSalary;      // 实发工资
}