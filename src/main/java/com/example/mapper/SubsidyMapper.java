package com.example.mapper;

import com.example.pojo.Subsidy;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SubsidyMapper {
    List<Subsidy> findAll();
    void insert(Subsidy subsidy);
    void update(Subsidy subsidy);
    void delete(Integer id);
}