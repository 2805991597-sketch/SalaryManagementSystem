package com.example.mapper;

import com.example.pojo.Level;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface LevelMapper {

    @Select("select * from level")
    List<Level> list();

    @Insert("insert into level(level_name, level_subsidy) values(#{levelName}, #{levelSubsidy})")
    void add(Level level);

    @Update("update level set level_name=#{levelName}, level_subsidy=#{levelSubsidy} where id=#{id}")
    void update(Level level);

    @Delete("delete from level where id=#{id}")
    void delete(Integer id);
}