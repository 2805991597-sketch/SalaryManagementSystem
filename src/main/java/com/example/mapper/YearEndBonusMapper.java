package com.example.mapper;

import com.example.pojo.YearEndBonus;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface YearEndBonusMapper {
    List<YearEndBonus> list(@Param("year") Integer year, @Param("empId") Integer empId, @Param("name") String name);
    YearEndBonus findById(@Param("id") Integer id);
    YearEndBonus findByEmpAndYear(@Param("empId") Integer empId, @Param("year") Integer year);
    void insert(YearEndBonus bonus);
    void update(YearEndBonus bonus);
    void delete(@Param("id") Integer id);
}