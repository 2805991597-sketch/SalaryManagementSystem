package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AttendanceDTO {
    private Integer id;

    @NotNull(message = "员工ID不能为空")
    private Integer empId;

    @NotNull(message = "月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式应为yyyy-MM")
    private String month;

    @Min(value = 0, message = "缺勤天数不能为负数")
    private Integer absentDays;

    @Min(value = 0, message = "迟到次数不能为负数")
    private Integer lateTimes;

    private java.math.BigDecimal fine;
    
    public void setFine(Object fine) {
        if (fine == null) {
            this.fine = null;
        } else if (fine instanceof java.math.BigDecimal) {
            this.fine = (java.math.BigDecimal) fine;
        } else if (fine instanceof Number) {
            this.fine = java.math.BigDecimal.valueOf(((Number) fine).doubleValue());
        } else if (fine instanceof String) {
            this.fine = new java.math.BigDecimal((String) fine);
        }
    }
}