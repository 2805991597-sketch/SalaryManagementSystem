package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RoleDTO {
    private Integer id;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[a-z]+$", message = "角色编码只能包含小写字母")
    private String roleCode;

    private String remark;
}
