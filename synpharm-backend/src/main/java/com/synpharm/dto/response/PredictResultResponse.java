package com.synpharm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预测结果响应DTO
 * 
 * <p>用于返回预测任务的结果数据。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@Builder
public class PredictResultResponse {

    /** 结果ID */
    private Long resultId;
    
    /** 靶点ID */
    private String targetId;
    
    /** 靶点名称 */
    private String targetName;
    
    /** 结合亲和力（负值表示亲和力强） */
    private Double bindingAffinity;
    
    /** 置信度分数（0-1） */
    private Double confidenceScore;
    
    /** 置信度等级（高/中/低） */
    private String confidenceLevel;
    
    /** 相互作用信息列表 */
    private List<InteractionInfo> interactions;
    
    /** 创建时间 */
    private LocalDateTime createdAt;

    /**
     * 相互作用信息内部类
     * 
     * <p>描述药物与靶点之间的具体相互作用。
     */
    @Data
    @Builder
    public static class InteractionInfo {
        /** 残基名称 */
        private String residue;
        
        /** 相互作用类型（如氢键、疏水作用等） */
        private String type;
        
        /** 距离（埃） */
        private Double distance;
    }
}