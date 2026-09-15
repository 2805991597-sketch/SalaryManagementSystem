package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryDTO {
    private Integer id;

    @NotNull(message = "员工ID不能为空")
    private Integer empId;

    @NotNull(message = "月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式应为yyyy-MM")
    private String month;

    private BigDecimal basicSalary;
    private BigDecimal levelSubsidy;
    private BigDecimal foodSubsidy;
    private BigDecimal trafficSubsidy;
    private BigDecimal housingSubsidy;
    private BigDecimal lateFine;
}