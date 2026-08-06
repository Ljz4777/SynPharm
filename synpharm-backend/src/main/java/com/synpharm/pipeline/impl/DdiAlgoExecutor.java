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
import java.util.Map;

/**
 * DDI（药物-药物相互作用）算法执行器。
 *
 * <p>与 DtiAlgoExecutor 同构：通过 FastApiClient 调用算法引擎，
 * 单条走 /v1/predict/single，批量走 /v1/predict/batch。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DdiAlgoExecutor implements AlgoExecutor {

    private final FastApiClient fastApiClient;

    @Override
    public AlgoType getAlgoType() {
        return AlgoType.DDI;
    }

    @Override
    public AlgoResponse execute(ParsedInput inputData) {
        log.info("执行DDI算法: params={}", inputData.getParams());

        List<String> params = inputData.getParams();
        PredictRequest request = PredictRequest.forDDI(params.get(0), params.get(1));

        AlgoResponse response = fastApiClient.predictSingle(request);
        response.setAlgoType(AlgoType.DDI.getCode());

        log.debug("DDI算法执行完成: confidenceScore={}",
                response.getMetrics() != null ? response.getMetrics().getConfidenceScore() : null);

        return response;
    }

    @Override
    public List<AlgoResponse> batchExecute(List<ParsedInput> inputDataList) {
        log.info("批量执行DDI算法: 数量={}", inputDataList.size());

        List<PredictRequest> requests = new ArrayList<>();
        for (ParsedInput input : inputDataList) {
            List<String> params = input.getParams();
            requests.add(PredictRequest.forDDI(params.get(0), params.get(1)));
        }

        BatchPredictionResponse batchResponse = fastApiClient.predictBatch(requests, AlgoType.DDI.getCode());

        List<AlgoResponse> responses = new ArrayList<>();
        if (batchResponse != null && batchResponse.getResults() != null) {
            for (Map<String, Object> result : batchResponse.getResults()) {
                responses.add(convertResult(result));
            }
        }

        log.debug("批量DDI算法执行完成: 结果数量={}", responses.size());
        return responses;
    }

    /**
     * 将 FastAPI 批量预测返回的平铺字段（snake_case）转换为统一的 AlgoResponse。
     */
    private AlgoResponse convertResult(Map<String, Object> result) {
        AlgoResponse response = new AlgoResponse();
        response.setStatus("success");
        response.setAlgoType(AlgoType.DDI.getCode());

        AlgoResponse.PredictionMetrics metrics = new AlgoResponse.PredictionMetrics();
        metrics.setTargetId(toStr(result.get("target_id")));
        metrics.setTargetName(toStr(result.get("target_name")));
        metrics.setBindingAffinity(toDouble(result.get("binding_affinity")));
        metrics.setConfidenceScore(toDouble(result.get("confidence_score")));
        metrics.setConfidenceLevel(toStr(result.get("confidence_level")));
        metrics.setInteractions(parseInteractions(result.get("interactions")));

        response.setMetrics(metrics);
        return response;
    }

    private String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str && !str.isEmpty()) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
                // 忽略无法解析的数值，返回 null
            }
        }
        return null;
    }

    private List<AlgoResponse.PredictionMetrics.InteractionInfo> parseInteractions(Object value) {
        List<AlgoResponse.PredictionMetrics.InteractionInfo> interactions = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    AlgoResponse.PredictionMetrics.InteractionInfo info =
                            new AlgoResponse.PredictionMetrics.InteractionInfo();
                    info.setResidue(toStr(map.get("residue")));
                    info.setType(toStr(map.get("type")));
                    info.setDistance(toDouble(map.get("distance")));
                    interactions.add(info);
                }
            }
        }
        return interactions;
    }
}
