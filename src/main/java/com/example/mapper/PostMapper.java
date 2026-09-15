package com.example.mapper;

import com.example.pojo.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> list(
            @Param("deptId") Integer deptId,
            @Param("postName") String postName
    );
    void insert(Post post);
    Post getById(Integer id);
    void add(Post post);
    void update(Post post);
    void delete(Integer id);
}