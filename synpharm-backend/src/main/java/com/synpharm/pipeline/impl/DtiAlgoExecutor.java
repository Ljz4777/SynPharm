package com.synpharm.pipeline.impl;

import com.synpharm.client.FastApiClient;
import com.synpharm.dto.ParsedInput;
import com.synpharm.dto.request.PredictRequest;
import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.BatchPredictionResponse;
import com.synpharm.enums.AlgoType;
import com.synpharm.pipeline.AlgoExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DtiAlgoExecutor implements AlgoExecutor {

    private final FastApiClient fastApiClient;

    @Override
    public AlgoType getAlgoType() {
        return AlgoType.DTI;
    }

    @Override
    public AlgoResponse execute(ParsedInput inputData) {
        log.info("执行DTI算法: params={}", inputData.getParams());
        
        List<String> params = inputData.getParams();
        PredictRequest request = PredictRequest.forDTI(params.get(0), params.get(1));
        
        AlgoResponse response = fastApiClient.predictSingle(request);
        response.setAlgoType(AlgoType.DTI.getCode());
        
        log.debug("DTI算法执行完成: confidenceScore={}", 
                response.getMetrics() != null ? response.getMetrics().getConfidenceScore() : null);
        
        return response;
    }

    @Override
    public List<AlgoResponse> batchExecute(List<ParsedInput> inputDataList) {
        log.info("批量执行DTI算法: 数量={}", inputDataList.size());
        
        List<PredictRequest> requests = new ArrayList<>();
        for (ParsedInput input : inputDataList) {
            List<String> params = input.getParams();
            requests.add(PredictRequest.forDTI(params.get(0), params.get(1)));
        }
        
        BatchPredictionResponse batchResponse = fastApiClient.predictBatch(requests, AlgoType.DTI.getCode());
        
        List<AlgoResponse> responses = new ArrayList<>();
        if (batchResponse != null && batchResponse.getResults() != null) {
            for (var result : batchResponse.getResults()) {
                AlgoResponse response = new AlgoResponse();
                response.setStatus("success");
                response.setAlgoType(AlgoType.DTI.getCode());
                response.setMetrics(result.getMetrics());
                responses.add(response);
            }
        }
        
        log.debug("批量DTI算法执行完成: 结果数量={}", responses.size());
        return responses;
    }
}