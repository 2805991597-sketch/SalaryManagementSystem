package com.example.service;

import com.example.mapper.SubsidyMapper;
import com.example.pojo.Subsidy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SubsidyService {
    @Resource
    SubsidyMapper mapper;

    public List<Subsidy> getAll() {
        return mapper.findAll();
    }

    @Transactional
    public void add(Subsidy subsidy) {
        log.info("新增补贴 - deptId:{}", subsidy.getDeptId());
        mapper.insert(subsidy);
    }

    @Transactional
    public void update(Subsidy subsidy) {
        log.info("更新补贴 - deptId:{}", subsidy.getDeptId());
        mapper.update(subsidy);
    }

    @Transactional
    public void delete(Integer id) {
        log.info("删除补贴 - id:{}", id);
        mapper.delete(id);
    }
}