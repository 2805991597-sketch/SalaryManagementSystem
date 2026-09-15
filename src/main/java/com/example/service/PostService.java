package com.example.service;

import com.example.mapper.PostMapper;
import com.example.pojo.Post;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PostService {

    @Resource
    private PostMapper postMapper;

    public List<Post> list(Integer deptId, String postName) {
        log.debug("查询职位列表 - deptId:{}, postName:{}", deptId, postName);
        return postMapper.list(deptId, postName);
    }

    public Post getById(Integer id) {
        log.debug("查询职位 - id:{}", id);
        return postMapper.getById(id);
    }

    @Transactional
    public void add(Post post) {
        log.info("新增职位 - {}", post.getPostName());
        postMapper.insert(post);
    }

    @Transactional
    public void update(Post post) {
        log.info("更新职位 - {}", post.getPostName());
        postMapper.update(post);
    }

    @Transactional
    public void delete(Integer id) {
        log.info("删除职位 - id:{}", id);
        postMapper.delete(id);
    }
}