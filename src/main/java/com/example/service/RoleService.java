package com.example.service;

import com.example.dto.RoleDTO;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.RoleMapper;
import com.example.pojo.Role;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RoleService {

    @Resource
    private RoleMapper roleMapper;

    public List<Role> list() {
        return roleMapper.list();
    }

    @Transactional
    public void save(RoleDTO dto) {
        log.info("保存角色 - id:{}, roleName:{}, roleCode:{}", dto.getId(), dto.getRoleName(), dto.getRoleCode());

        Role existing = roleMapper.findByCode(dto.getRoleCode());
        if (existing != null && (dto.getId() == null || !existing.getId().equals(dto.getId()))) {
            throw new IllegalArgumentException("角色编码已存在");
        }

        Role role = new Role();
        role.setId(dto.getId());
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setRemark(dto.getRemark());

        if (role.getId() == null) {
            roleMapper.insert(role);
            log.info("角色新增成功 - roleCode:{}", dto.getRoleCode());
        } else {
            roleMapper.update(role);
            log.info("角色更新成功 - id:{}, roleCode:{}", role.getId(), dto.getRoleCode());
        }
    }

    @Transactional
    public void delete(Integer id) {
        log.debug("删除角色 - id:{}", id);
        Role role = roleMapper.findById(id);
        if (role == null) {
            throw new ResourceNotFoundException("角色不存在");
        }
        roleMapper.delete(id);
        log.info("角色删除成功 - id:{}", id);
    }
}
