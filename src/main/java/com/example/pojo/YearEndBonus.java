package com.example.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class YearEndBonus {
    private Integer id;
    private Integer empId;
    private Integer year;
    private BigDecimal bonusAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String name;
    private String deptName;

    private BigDecimal yearSalary;
    private BigDecimal yearBonus;
    private BigDecimal yearTax;
    private BigDecimal yearActual;
}