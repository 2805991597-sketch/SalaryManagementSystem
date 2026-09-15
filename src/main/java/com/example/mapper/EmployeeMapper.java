package com.example.mapper;

import com.example.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeMapper {
    // 查询所有员工
    List<Employee> list(
            @Param("deptId") Integer deptId,
            @Param("postId") Integer postId,
            @Param("name") String name
    );

    // 新增员工
    void insert(Employee e);

    // 修改员工
    void update(Employee e);

    // 删除员工
    void delete(Integer id);

    // 根据ID查询员工
    Employee findById(Integer id);

    // 根据用户名查询员工
    Employee findByUsername(@Param("username") String username);

    // 根据部门ID查询员工数量
    int countByDeptId(@Param("deptId") Integer deptId);

    // 检查员工关联的考勤记录数量
    int countAttendanceByEmpId(@Param("empId") Integer empId);

    // 检查员工关联的薪资记录数量
    int countSalaryByEmpId(@Param("empId") Integer empId);

    // 检查员工关联的年终奖记录数量
    int countYearEndBonusByEmpId(@Param("empId") Integer empId);
}