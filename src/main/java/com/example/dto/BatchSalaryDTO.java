package com.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchSalaryDTO {
    private String mode; // "single" 或 "year"
    private String month; // 单月模式时使用
    private Integer year; // 全年模式时使用
    private List<Integer> empIds; // 员工ID列表
}
