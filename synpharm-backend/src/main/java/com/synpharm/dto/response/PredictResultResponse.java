package com.synpharm.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预测结果响应DTO
 *
 * <p>用于返回预测结果数据，字段与前端渲染契约对齐
 * （id / ligandSmiles / datasetInfo / interactions）。
 *
 * @author SynPharm Team
 * @version 1.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictResultResponse {

    /** 结果ID（历史/结果查询时有值，实时预测可能为 null） */
    private Long id;

    /** 算法类型（DTI/PPI/DDI） */
    private String algoType;

    /** 靶点ID */
    private String targetId;

    /** 靶点名称 */
    private String targetName;

    /** 配体SMILES（DTI/DDI 有值，PPI 为 null） */
    private String ligandSmiles;

    /** 结合亲和力 */
    private Double bindingAffinity;

    /** 置信度分数（0-1） */
    private Double confidenceScore;

    /** 置信度等级（high/medium/low） */
    private String confidenceLevel;

    /** 相互作用信息列表 */
    private List<InteractionInfo> interactions;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 数据集信息 */
    private DatasetInfo datasetInfo;

    /**
     * 相互作用信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractionInfo {
        /** 相互作用类型 */
        private String type;
        /** 残基名称 */
        private String residueName;
        /** 残基编号（FastAPI 未提供，可为 null） */
        private String residueNumber;
        /** 距离（埃） */
        private Double distance;
    }

    /**
     * 数据集信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasetInfo {
        private String name;
        private Integer size;
        private String description;
        private String source;
    }
}