package com.synpharm.service.impl;

import com.synpharm.client.FastApiClient;
import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.dto.request.PredictRequest;
import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.service.PredictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictServiceImpl implements PredictService {

    private final FastApiClient fastApiClient;

    @Override
    public PredictResultResponse predictDTI(DTIPredictRequest request, Long userId) {
        log.info("DTI预测请求: userId={}, smiles={}, targetId={}", userId, request.getSmiles(), request.getTargetId());

        PredictRequest predictRequest = PredictRequest.forDTI(request.getSmiles(), request.getTargetId());
        AlgoResponse response = fastApiClient.predictSingle(predictRequest);

        return convertToResponse(response, "DTI");
    }

    @Override
    public PredictResultResponse predictPPI(PPIPredictRequest request, Long userId) {
        log.info("PPI预测请求: userId={}", userId);

        PredictRequest predictRequest = PredictRequest.forPPI(request.getProteinA(), request.getProteinB());
        AlgoResponse response = fastApiClient.predictSingle(predictRequest);

        return convertToResponse(response, "PPI");
    }

    @Override
    public PredictResultResponse predictDDI(DDIPredictRequest request, Long userId) {
        log.info("DDI预测请求: userId={}", userId);

        PredictRequest predictRequest = PredictRequest.forDDI(request.getDrugA(), request.getDrugB());
        AlgoResponse response = fastApiClient.predictSingle(predictRequest);

        return convertToResponse(response, "DDI");
    }

    private PredictResultResponse convertToResponse(AlgoResponse response, String algoType) {
        if (response == null || response.getMetrics() == null) {
            throw new RuntimeException("预测结果为空");
        }

        var metrics = response.getMetrics();
        List<PredictResultResponse.InteractionInfo> interactions = new ArrayList<>();

        if (metrics.getInteractions() != null) {
            for (var interaction : metrics.getInteractions()) {
                interactions.add(PredictResultResponse.InteractionInfo.builder()
                        .residue(interaction.getResidue())
                        .type(interaction.getType())
                        .distance(interaction.getDistance())
                        .build());
            }
        }

        return PredictResultResponse.builder()
                .algoType(algoType)
                .targetId(metrics.getTargetId())
                .targetName(metrics.getTargetName())
                .bindingAffinity(metrics.getBindingAffinity())
                .confidenceScore(metrics.getConfidenceScore())
                .confidenceLevel(metrics.getConfidenceLevel())
                .interactions(interactions)
                .build();
    }
}