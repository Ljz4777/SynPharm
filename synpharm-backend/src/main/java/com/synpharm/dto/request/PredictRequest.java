package com.synpharm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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