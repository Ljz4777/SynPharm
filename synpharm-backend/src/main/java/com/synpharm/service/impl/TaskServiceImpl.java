package com.synpharm.service.impl;

import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.PredictTask;
import com.synpharm.repository.mapper.PredictTaskMapper;
import com.synpharm.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 任务服务实现类
 *
 * <p>实现预测任务的管理操作，包括任务创建、查询、状态更新。
 * 查询/取消/删除带归属校验（B-03）：只能操作自己的任务。
 * 历史遗留的 executeTask + PredictUtils 假数据链路（C-05/C-06）已删除。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final PredictTaskMapper taskMapper;

    @Override
    @Transactional
    public PredictTask createTask(Long userId, String predictType, String inputType, String inputValue, String fileUrl) {
        PredictTask task = new PredictTask();
        task.setTaskNo(generateTaskNo());
        task.setUserId(userId);
        task.setPredictType(predictType);
        task.setInputType(inputType);
        task.setInputValue(inputValue);
        task.setFileUrl(fileUrl);
        task.setStatus("pending");
        task.setProgress(0);

        taskMapper.insert(task);
        log.info("创建任务成功: taskNo={}", task.getTaskNo());
        return task;
    }

    @Override
    public PredictTask getTaskById(Long taskId, Long userId) {
        PredictTask task = findTaskOrThrow(taskId);
        checkOwnership(task, userId);
        return task;
    }

    @Override
    public PredictTask getTaskByNo(String taskNo) {
        return taskMapper.selectByTaskNo(taskNo);
    }

    @Override
    public List<PredictTask> getTasksByUserId(Long userId) {
        return taskMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long taskId, String status) {
        PredictTask task = findTaskOrThrow(taskId);
        task.setStatus(status);
        taskMapper.updateById(task);
        log.info("更新任务状态: taskId={}, status={}", taskId, status);
    }

    @Override
    @Transactional
    public void updateTaskProgress(Long taskId, Integer progress) {
        PredictTask task = findTaskOrThrow(taskId);
        task.setProgress(progress);
        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void cancelTask(Long taskId, Long userId) {
        PredictTask task = findTaskOrThrow(taskId);
        checkOwnership(task, userId);
        if ("running".equals(task.getStatus())) {
            task.setStatus("cancelled");
            taskMapper.updateById(task);
            log.info("取消任务: taskId={}", taskId);
        }
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        PredictTask task = findTaskOrThrow(taskId);
        checkOwnership(task, userId);
        taskMapper.deleteById(taskId);
        log.info("删除任务: taskId={}", taskId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 按主键查询任务，不存在抛 TASK_NOT_FOUND（内部使用，不做归属校验）
     */
    private PredictTask findTaskOrThrow(Long taskId) {
        PredictTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    /**
     * 归属校验（B-03，fail-closed）：
     * userId 为空 -> UNAUTHORIZED；任务不属于该用户 -> FORBIDDEN
     */
    private void checkOwnership(PredictTask task, Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        if (!userId.equals(task.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该任务");
        }
    }

    /**
     * 生成任务编号
     *
     * @return 任务编号
     */
    private String generateTaskNo() {
        return "TASK" + System.currentTimeMillis();
    }
}
