package com.synpharm.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测结果实体
 * 
 * <p>映射数据库表 predict_result，存储预测结果信息。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@TableName("predict_result")
public class PredictResult {

    /** 结果ID（主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务ID */
    private Long taskId;
    
    /** 靶点ID */
    private String targetId;
    
    /** 靶点名称 */
    private String targetName;
    
    /** 结合亲和力 */
    private Double bindingAffinity;
    
    /** 置信度分数 */
    private Double confidenceScore;
    
    /** 置信度等级 */
    private String confidenceLevel;
    
    /** 相互作用信息（JSON格式） */
    private String interactions;
    
    /** 结果数据（JSON格式） */
    private String resultData;

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