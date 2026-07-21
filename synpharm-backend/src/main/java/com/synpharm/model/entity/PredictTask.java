package com.synpharm.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测任务实体
 * 
 * <p>映射数据库表 predict_task，存储预测任务信息。
 * 
 * @author SynPharm Team
 * @version 1.0.0
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

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 删除标记（0未删除，1已删除） */
    @TableLogic
    private Integer deleted;
}