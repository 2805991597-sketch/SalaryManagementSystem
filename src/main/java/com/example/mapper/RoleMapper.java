package com.example.mapper;

import com.example.pojo.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMapper {
    List<Role> list();

    int insert(Role role);

    int update(Role role);

    void delete(Integer id);

    Role findById(Integer id);

    Role findByCode(@Param("code") String code);
}
