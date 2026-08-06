package com.synpharm.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送给 FastAPI 算法引擎的预测请求 DTO。
 *
 * <p>FastAPI 侧 Pydantic 模型使用 snake_case（algo_type/drug_smiles/...），
 * 故通过 @JsonNaming(SnakeCaseStrategy) 将 camelCase 字段序列化为 snake_case，
 * 与 FastAPI 契约对齐。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PredictRequest {

    private String algoType;
    
    private String drugSmiles;
    
    private String targetSeq;
    
    private String proteinA;
    
    private String proteinB;
    
    private String drugA;
    
    private String drugB;

    public static PredictRequest forDTI(String drugSmiles, String targetSeq) {
        return PredictRequest.builder()
                .algoType("DTI")
                .drugSmiles(drugSmiles)
                .targetSeq(targetSeq)
                .build();
    }

    public static PredictRequest forPPI(String proteinA, String proteinB) {
        return PredictRequest.builder()
                .algoType("PPI")
                .proteinA(proteinA)
                .proteinB(proteinB)
                .build();
    }

    public static PredictRequest forDDI(String drugA, String drugB) {
        return PredictRequest.builder()
                .algoType("DDI")
                .drugA(drugA)
                .drugB(drugB)
                .build();
    }
}