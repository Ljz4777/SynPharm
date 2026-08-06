package com.synpharm.pipeline.impl;

import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.enums.OutputType;
import com.synpharm.pipeline.OutputFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JsonOutputFormatter implements OutputFormatter {

    @Override
    public OutputType getOutputType() {
        return OutputType.JSON;
    }

    @Override
    public PredictResultResponse format(AlgoResponse response) {
        if (response == null || response.getMetrics() == null) {
            throw new RuntimeException("预测结果为空");
        }
        
        log.debug("格式化JSON输出: status={}, algoType={}", response.getStatus(), response.getAlgoType());
        
        return buildResponse(response);
    }

    @Override
    public List<PredictResultResponse> batchFormat(List<AlgoResponse> resultDataList) {
        log.debug("批量格式化JSON输出: 数量={}", resultDataList.size());
        
        List<PredictResultResponse> responses = new ArrayList<>();
        for (AlgoResponse response : resultDataList) {
            try {
                responses.add(buildResponse(response));
            } catch (Exception e) {
                log.warn("格式化单个结果失败", e);
            }
        }
        return responses;
    }

    private PredictResultResponse buildResponse(AlgoResponse response) {
        var metrics = response.getMetrics();
        List<PredictResultResponse.InteractionInfo> interactions = new ArrayList<>();
        
        if (metrics.getInteractions() != null) {
            for (var interaction : metrics.getInteractions()) {
                interactions.add(PredictResultResponse.InteractionInfo.builder()
                        .type(interaction.getType())
                        .residueName(interaction.getResidue())
                        .distance(interaction.getDistance())
                        .build());
            }
        }

        String algoType = response.getAlgoType();
        return PredictResultResponse.builder()
                .algoType(algoType)
                .targetId(metrics.getTargetId())
                .targetName(metrics.getTargetName())
                .bindingAffinity(metrics.getBindingAffinity())
                .confidenceScore(metrics.getConfidenceScore())
                .confidenceLevel(metrics.getConfidenceLevel())
                .interactions(interactions)
                .datasetInfo(PredictResultResponse.DatasetInfo.builder()
                        .name(algoType == null ? "AI预测" : algoType + "预测结果")
                        .size(metrics.getInteractions() == null ? 0 : metrics.getInteractions().size())
                        .description("由 FastAPI 算法引擎计算")
                        .source("fastapi")
                        .build())
                .build();
    }
}