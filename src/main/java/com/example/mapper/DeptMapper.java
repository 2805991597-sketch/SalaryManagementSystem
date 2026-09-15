package com.example.mapper;

import com.example.pojo.Dept;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DeptMapper {
    List<Dept> findAll();
    List<Dept> findByDeptName(String deptName);
    void insert(Dept dept);
    void update(Dept dept);
    void delete(Integer id);
}