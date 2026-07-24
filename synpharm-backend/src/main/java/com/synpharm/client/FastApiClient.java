package com.synpharm.client;

import com.synpharm.dto.request.PredictRequest;
import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.BatchPredictionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FastApiClient {

    private final WebClient fastApiWebClient;
    
    private final Duration singleTimeout;
    
    private final Duration batchTimeout;

    public AlgoResponse predictSingle(PredictRequest request) {
        log.info("调用FastAPI单条预测: algoType={}", request.getAlgoType());
        try {
            return fastApiWebClient.post()
                    .uri("/v1/predict/single")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AlgoResponse.class)
                    .timeout(singleTimeout)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("FastAPI单条预测HTTP错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("预测服务调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("FastAPI单条预测调用失败", e);
            throw new RuntimeException("预测服务调用失败");
        }
    }

    public BatchPredictionResponse predictBatch(List<PredictRequest> requestList, String algoType) {
        log.info("调用FastAPI批量预测: {}条数据, algoType={}", requestList.size(), algoType);
        try {
            return fastApiWebClient.post()
                    .uri("/v1/predict/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("data_list", requestList, "algo_type", algoType))
                    .retrieve()
                    .bodyToMono(BatchPredictionResponse.class)
                    .timeout(batchTimeout)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("FastAPI批量预测HTTP错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("批量预测服务调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("FastAPI批量预测调用失败", e);
            throw new RuntimeException("批量预测服务调用失败");
        }
    }
}