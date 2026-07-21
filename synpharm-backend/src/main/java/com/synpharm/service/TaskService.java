package com.synpharm.service;

import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.model.entity.PredictTask;

import java.util.List;

/**
 * 任务服务接口
 * 
 * <p>定义预测任务的管理操作，包括任务创建、查询、状态更新等。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
public interface TaskService {

    /**
     * 创建预测任务
     * 
     * @param userId 用户ID
     * @param predictType 预测类型
     * @param inputType 输入类型
     * @param inputValue 输入值
     * @param fileUrl 文件URL（可选）
     * @return 任务实体
     */
    PredictTask createTask(Long userId, String predictType, String inputType, String inputValue, String fileUrl);

    /**
     * 根据任务ID查询任务
     * 
     * @param taskId 任务ID
     * @return 任务实体
     */
    PredictTask getTaskById(Long taskId);

    /**
     * 根据任务编号查询任务
     * 
     * @param taskNo 任务编号
     * @return 任务实体
     */
    PredictTask getTaskByNo(String taskNo);

    /**
     * 根据用户ID查询任务列表
     * 
     * @param userId 用户ID
     * @return 任务列表
     */
    List<PredictTask> getTasksByUserId(Long userId);

    /**
     * 更新任务状态
     * 
     * @param taskId 任务ID
     * @param status 状态
     */
    void updateTaskStatus(Long taskId, String status);

    /**
     * 更新任务进度
     * 
     * @param taskId 任务ID
     * @param progress 进度（0-100）
     */
    void updateTaskProgress(Long taskId, Integer progress);

    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);

    /**
     * 删除任务
     * 
     * @param taskId 任务ID
     */
    void deleteTask(Long taskId);

    /**
     * 执行任务预测
     * 
     * @param taskId 任务ID
     * @return 预测结果列表
     */
    List<PredictResultResponse> executeTask(Long taskId);
}