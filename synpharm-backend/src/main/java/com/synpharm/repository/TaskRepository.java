package com.synpharm.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.synpharm.model.entity.PredictTask;
import com.synpharm.repository.mapper.PredictTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 任务仓储
 */
@Repository
@RequiredArgsConstructor
public class TaskRepository {

    private final PredictTaskMapper predictTaskMapper;

    public PredictTask findById(Long id) {
        return predictTaskMapper.selectById(id);
    }

    public PredictTask findByTaskNo(String taskNo) {
        LambdaQueryWrapper<PredictTask> query = Wrappers.lambdaQuery();
        query.eq(PredictTask::getTaskNo, taskNo);
        query.eq(PredictTask::getDeleted, 0);
        return predictTaskMapper.selectOne(query);
    }

    public void save(PredictTask task) {
        if (task.getId() == null) {
            predictTaskMapper.insert(task);
        } else {
            predictTaskMapper.updateById(task);
        }
    }

    public void deleteById(Long id) {
        predictTaskMapper.deleteById(id);
    }
}