package com.example.mapper;

import com.example.pojo.TaxDetail;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface TaxDetailMapper {
    List<TaxDetail> list(@Param("taxType") String taxType, @Param("isActive") Boolean isActive);
    TaxDetail findById(@Param("id") Integer id);
    void insert(TaxDetail taxDetail);
    void update(TaxDetail taxDetail);
    void delete(@Param("id") Integer id);
}