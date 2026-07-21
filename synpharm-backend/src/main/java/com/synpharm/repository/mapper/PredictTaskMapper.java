package com.synpharm.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synpharm.model.entity.PredictTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预测任务数据访问接口
 * 
 * <p>继承MyBatisPlus的BaseMapper，提供预测任务数据的CRUD操作。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Mapper
public interface PredictTaskMapper extends BaseMapper<PredictTask> {
    
    /**
     * 根据用户ID查询任务列表
     * 
     * @param userId 用户ID
     * @return 任务列表
     */
    List<PredictTask> selectByUserId(Long userId);
    
    /**
     * 根据任务编号查询任务
     * 
     * @param taskNo 任务编号
     * @return 任务实体
     */
    PredictTask selectByTaskNo(String taskNo);
}