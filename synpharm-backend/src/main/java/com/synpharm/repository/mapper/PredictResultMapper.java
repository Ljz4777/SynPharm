package com.synpharm.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synpharm.model.entity.PredictResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预测结果数据访问接口
 * 
 * <p>继承MyBatisPlus的BaseMapper，提供预测结果数据的CRUD操作。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Mapper
public interface PredictResultMapper extends BaseMapper<PredictResult> {
    
    /**
     * 根据任务ID查询结果列表
     * 
     * @param taskId 任务ID
     * @return 结果列表
     */
    List<PredictResult> selectByTaskId(Long taskId);
}