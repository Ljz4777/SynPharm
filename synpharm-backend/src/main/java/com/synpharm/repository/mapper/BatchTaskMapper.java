package com.synpharm.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synpharm.model.entity.BatchTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BatchTaskMapper extends BaseMapper<BatchTask> {

    BatchTask selectByBatchId(String batchId);

    List<BatchTask> selectByUserId(Long userId);
}
