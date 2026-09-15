package com.example.service;

import com.example.pojo.Level;
import com.example.mapper.LevelMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class LevelService {

    @Resource
    private LevelMapper levelMapper;

    public List<Level> list() {
        log.debug("查询等级列表");
        return levelMapper.list();
    }

    @Transactional
    public void add(Level level) {
        log.info("新增等级 - {}", level.getLevelName());
        levelMapper.add(level);
    }

    @Transactional
    public void update(Level level) {
        log.info("更新等级 - {}", level.getLevelName());
        levelMapper.update(level);
    }

    @Transactional
    public void delete(Integer id) {
        log.info("删除等级 - id:{}", id);
        levelMapper.delete(id);
    }
}