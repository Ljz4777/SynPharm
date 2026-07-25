package com.synpharm.pipeline.impl;

import com.synpharm.client.FastApiClient;
import com.synpharm.dto.request.PredictRequest;
import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.enums.AlgoType;
import com.synpharm.pipeline.AlgoExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DtiAlgoExecutor implements AlgoExecutor<PredictRequest, AlgoResponse> {

    private final FastApiClient fastApiClient;

    @Override
    public AlgoType getAlgoType() {
        return AlgoType.DTI;
    }

    @Override
    public AlgoResponse execute(PredictRequest inputData) {
        log.info("执行DTI算法: drugSmiles={}, targetSeq={}", inputData.getDrugSmiles(), inputData.getTargetSeq());
        
        AlgoResponse response = fastApiClient.predictSingle(inputData);
        response.setAlgoType(AlgoType.DTI.getCode());
        
        log.debug("DTI算法执行完成: confidenceScore={}", 
                response.getMetrics() != null ? response.getMetrics().getConfidenceScore() : null);
        
        return response;
    }
}