package com.example.pojo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Subsidy {
    private Integer id;
    private Integer deptId;
    private String deptName;
    private BigDecimal foodSubsidy;
    private BigDecimal trafficSubsidy;
    private BigDecimal housingSubsidy;
}