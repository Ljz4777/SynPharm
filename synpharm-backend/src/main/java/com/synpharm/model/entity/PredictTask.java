package com.synpharm.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测任务实体
 *
 * <p>映射数据库表 predict_task（v3.0.0），存储预测任务信息。
 * 字段与建表脚本 03_predict_task.sql 保持一致。
 *
 * @author SynPharm Team
 * @version 1.1.0
 */
@Data
@TableName("predict_task")
public class PredictTask {

    /** 任务ID（主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务编号（唯一标识） */
    private String taskNo;
    
    /** 用户ID */
    private Long userId;
    
    /** 预测类型（dti/ppi/ddi） */
    private String predictType;
    
    /** 输入类型 */
    private String inputType;
    
    /** 输入值 */
    private String inputValue;
    
    /** 上传文件URL */
    private String fileUrl;
    
    /** 任务状态（pending/running/completed/failed/cancelled） */
    private String status;
    
    /** 任务进度（0-100） */
    private Integer progress;

    /** 错误信息 */
    private String errorMessage;

    /** AI服务任务ID */
    private String aiTaskId;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 开始时间 */
    private LocalDateTime startedAt;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 删除标记（0未删除，1已删除） */
    @TableLogic
    private Integer deleted;
}