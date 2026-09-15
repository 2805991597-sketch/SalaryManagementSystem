package com.example.pojo;

import lombok.Data;

@Data
public class Post {
    private Integer id;
    private String postName;
    private Integer deptId;
    private String deptName;
    private Double baseSalary;
}