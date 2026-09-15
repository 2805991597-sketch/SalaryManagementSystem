package com.example.pojo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Employee {
    private Integer id;
    private String name;
    private Integer deptId;
    private String deptName;
    private Integer postId;
    private String postName;
    private BigDecimal postBaseSalary;
    private Integer levelId;
    private String levelName;
    private BigDecimal personalSalary;
    private Integer roleId;
    private String roleName;
    private String roleCode;
    private String username;
    private String password;
}