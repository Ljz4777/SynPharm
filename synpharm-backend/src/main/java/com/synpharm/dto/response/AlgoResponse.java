package com.synpharm.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AlgoResponse {

    private String status;
    private String algoType;
    private PredictionMetrics metrics;

    @Data
    public static class PredictionMetrics {
        private String targetId;
        private String targetName;
        private Double bindingAffinity;
        private Double confidenceScore;
        private String confidenceLevel;
        private List<InteractionInfo> interactions;

        @Data
        public static class InteractionInfo {
            private String residue;
            private String type;
            private Double distance;
        }
    }
}
