package com.synpharm.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

/**
 * FastAPI 算法引擎响应 DTO。
 *
 * <p>FastAPI 返回 snake_case 字段（target_id/binding_affinity/...），
 * 通过 @JsonNaming(SnakeCaseStrategy) 在反序列化时映射为 camelCase 属性。
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AlgoResponse {

    private String status;
    private String algoType;
    private PredictionMetrics metrics;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PredictionMetrics {
        private String targetId;
        private String targetName;
        private Double bindingAffinity;
        private Double confidenceScore;
        private String confidenceLevel;
        private List<InteractionInfo> interactions;

        @Data
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class InteractionInfo {
            private String residue;
            private String type;
            private Double distance;
        }
    }
}
