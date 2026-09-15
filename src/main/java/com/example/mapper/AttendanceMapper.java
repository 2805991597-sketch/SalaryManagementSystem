package com.example.mapper;

import com.example.pojo.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttendanceMapper {
    List<Attendance> list(
            @Param("year") String year,
            @Param("month") String month,
            @Param("dept") String dept,
            @Param("name") String name,
            @Param("empId") Integer empId,
            @Param("sortOrder") String sortOrder
    );

    int insert(Attendance attendance);

    int update(Attendance attendance);

    void delete(Integer id);

    Attendance selectByEmpAndMonth(@Param("empId") Integer empId, @Param("month") java.time.LocalDate month);

    Attendance findById(Integer id);
}