package com.synpharm.service.impl;

import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.service.PredictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 预测服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictServiceImpl implements PredictService {

    private final Random random = new Random();

    @Override
    public PredictResultResponse predictDTI(DTIPredictRequest request, Long userId) {
        log.info("DTI预测请求: userId={}, smiles={}, targetId={}", userId, request.getSmiles(), request.getTargetId());
        return generateMockResult("DTI", request.getTargetId());
    }

    @Override
    public PredictResultResponse predictPPI(PPIPredictRequest request, Long userId) {
        log.info("PPI预测请求: userId={}", userId);
        return generateMockResult("PPI", "PPI_TARGET");
    }

    @Override
    public PredictResultResponse predictDDI(DDIPredictRequest request, Long userId) {
        log.info("DDI预测请求: userId={}", userId);
        return generateMockResult("DDI", "DDI_TARGET");
    }

    private PredictResultResponse generateMockResult(String type, String targetId) {
        double bindingAffinity = -5.0 - random.nextDouble() * 10;
        double confidenceScore = 0.7 + random.nextDouble() * 0.3;
        String confidenceLevel = confidenceScore >= 0.9 ? "高" : confidenceScore >= 0.8 ? "中" : "低";

        List<PredictResultResponse.InteractionInfo> interactions = new ArrayList<>();
        interactions.add(PredictResultResponse.InteractionInfo.builder()
                .residue("ASP123")
                .type("氢键")
                .distance(2.8 + random.nextDouble() * 0.5)
                .build());
        interactions.add(PredictResultResponse.InteractionInfo.builder()
                .residue("LYS456")
                .type("疏水作用")
                .distance(3.5 + random.nextDouble() * 1.0)
                .build());

        return PredictResultResponse.builder()
                .targetId(targetId)
                .targetName("目标蛋白")
                .bindingAffinity(Math.round(bindingAffinity * 100.0) / 100.0)
                .confidenceScore(Math.round(confidenceScore * 100.0) / 100.0)
                .confidenceLevel(confidenceLevel)
                .interactions(interactions)
                .build();
    }
}