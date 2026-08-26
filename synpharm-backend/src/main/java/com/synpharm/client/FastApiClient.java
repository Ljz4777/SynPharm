package com.synpharm.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpharm.dto.request.PredictRequest;
import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.BatchPredictionResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
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

    private final ObjectMapper objectMapper;

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
            throw translateError(e);
        } catch (Exception e) {
            log.error("FastAPI单条预测调用失败", e);
            throw new BusinessException(ErrorCode.PREDICT_ERROR, "预测服务不可用，请稍后重试");
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
            throw translateError(e);
        } catch (Exception e) {
            log.error("FastAPI批量预测调用失败", e);
            throw new BusinessException(ErrorCode.PREDICT_ERROR, "批量预测服务不可用，请稍后重试");
        }
    }

    /**
     * 把 FastAPI 返回的 4xx/5xx 错误转成业务异常，透传 FastAPI 响应体里的 detail 字段。
     * <p>例如权重未就绪返回 404、输入非法返回 400，此处不再吞成笼统的"系统错误"。
     */
    private RuntimeException translateError(WebClientResponseException e) {
        String detail = extractDetail(e.getResponseBodyAsString());
        int status = e.getStatusCode().value();

        if (status == 404) {
            return new BusinessException(ErrorCode.NOT_FOUND, detail != null ? detail : "预测模型未就绪");
        }
        if (status == 400 || status == 422) {
            return new BusinessException(ErrorCode.BAD_REQUEST, detail != null ? detail : "预测输入无效");
        }
        return new BusinessException(ErrorCode.SYSTEM_ERROR, "预测服务异常，请稍后重试");
    }

    /**
     * 从 FastAPI 错误响应体 {"status":"error","detail":"..."} 中提取 detail 字段。
     */
    private String extractDetail(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode detail = node.get("detail");
            return (detail != null && !detail.isNull()) ? detail.asText() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
