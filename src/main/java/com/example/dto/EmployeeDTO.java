package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmployeeDTO {
    private Integer id;

    @NotBlank(message = "员工姓名不能为空")
    private String name;

    @NotNull(message = "部门ID不能为空")
    private Integer deptId;

    @NotNull(message = "职位ID不能为空")
    private Integer postId;

    private Integer levelId;

    @NotNull(message = "角色ID不能为空")
    private Integer roleId;

    @NotBlank(message = "登录账号不能为空")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    private BigDecimal personalSalary;
}