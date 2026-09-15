package com.example.controller;

import com.example.pojo.Post;
import com.example.pojo.Result;
import com.example.service.PostService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/post")
public class PostController {

    @Resource
    private PostService postService;

    @GetMapping("/list")
    public Result<List<Post>> list(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) String postName
    ) {
        log.info("查询职位列表 - deptId:{}, postName:{}", deptId, postName);
        List<Post> list = postService.list(deptId, postName);
        return Result.success(list);
    }

    @GetMapping("/getById")
    public Result<Post> getById(@RequestParam Integer id) {
        log.info("查询职位 - id:{}", id);
        Post post = postService.getById(id);
        return Result.success(post);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Post post) {
        log.info("新增职位 - {}", post.getPostName());
        postService.add(post);
        return Result.successMsg("添加成功");
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody Post post) {
        log.info("更新职位 - {}", post.getPostName());
        postService.update(post);
        return Result.successMsg("修改成功");
    }

    @GetMapping("/delete")
    public Result<Void> delete(@RequestParam Integer id) {
        log.info("删除职位 - id:{}", id);
        postService.delete(id);
        return Result.successMsg("删除成功");
    }
}