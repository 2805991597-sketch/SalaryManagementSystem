package com.example.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaxDetail {
    private Integer id;
    private String taxType;
    private String taxName;
    private BigDecimal rate;
    private BigDecimal quickDeduction;
    private BigDecimal minThreshold;
    private BigDecimal maxLimit;
    private String description;
    private Integer effectiveYear;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}