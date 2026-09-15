package com.example.service;

import com.example.mapper.DeptMapper;
import com.example.mapper.EmployeeMapper;
import com.example.pojo.Dept;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DeptService {
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private EmployeeMapper employeeMapper;

    public List<Dept> getAll() {
        return deptMapper.findAll();
    }

    public List<Dept> search(String deptName) {
        log.debug("查询部门 - deptName:{}", deptName);
        if (deptName == null || deptName.isEmpty()) {
            return getAll();
        }
        return deptMapper.findByDeptName(deptName);
    }

    @Transactional
    public void add(Dept dept) {
        log.info("新增部门 - {}", dept.getDeptName());
        deptMapper.insert(dept);
    }

    @Transactional
    public void update(Dept dept) {
        log.info("更新部门 - {}", dept.getDeptName());
        deptMapper.update(dept);
    }

    @Transactional
    public void delete(Integer id) {
        log.info("删除部门 - id:{}", id);
        
        // 检查该部门是否有员工
        int count = employeeMapper.countByDeptId(id);
        if (count > 0) {
            throw new RuntimeException("该部门下还有员工，无法删除");
        }
        
        deptMapper.delete(id);
    }
}