package com.example.pojo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Level {
    private Integer id;
    private String levelName;   // 职级名称
    private BigDecimal levelSubsidy; // 职级补贴
}