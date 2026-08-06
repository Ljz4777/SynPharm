package com.synpharm.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测结果实体
 *
 * <p>映射数据库表 predict_result（v3.0.0），存储预测结果信息。
 * 字段与建表脚本 04_predict_result.sql 保持一致。
 *
 * @author SynPharm Team
 * @version 1.1.0
 */
@Data
@TableName("predict_result")
public class PredictResult {

    /** 结果ID（主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 结果编号（唯一） */
    private String resultNo;

    /** 任务ID */
    private Long taskId;

    /** 用户ID */
    private Long userId;

    /** 靶点ID */
    private String targetId;

    /** 靶点名称 */
    private String targetName;

    /** 配体SMILES */
    private String ligandSmiles;

    /** 结合亲和力 */
    private Double bindingAffinity;

    /** 置信度分数 */
    private Double confidenceScore;

    /** 置信度等级 */
    private String confidenceLevel;

    /** 完整预测数据（JSON） */
    private String predictionData;

    /** 相互作用信息（JSON） */
    private String interactions;

    /** 数据集来源 */
    private String datasetSource;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 删除标记（0未删除，1已删除） */
    @TableLogic
    private Integer deleted;
}