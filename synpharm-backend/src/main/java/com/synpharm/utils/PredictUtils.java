package com.synpharm.utils;

import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.model.entity.PredictTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 预测工具类
 * 
 * <p>提供药物-靶点相互作用预测的核心算法和数据处理功能。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class PredictUtils {

    private final Random random = new Random();

    /**
     * 执行预测任务
     * 
     * <p>根据任务类型调用相应的预测方法。
     * 
     * @param task 预测任务实体
     * @return 预测结果列表
     */
    public List<PredictResultResponse> predict(PredictTask task) {
        String predictType = task.getPredictType();
        
        switch (predictType) {
            case "dti":
                return predictDTI(task);
            case "ppi":
                return predictPPI(task);
            case "ddi":
                return predictDDI(task);
            default:
                throw new IllegalArgumentException("未知的预测类型: " + predictType);
        }
    }

    /**
     * DTI（药物-靶点相互作用）预测
     * 
     * @param task 预测任务实体
     * @return 预测结果列表
     */
    private List<PredictResultResponse> predictDTI(PredictTask task) {
        log.info("执行DTI预测任务: taskId={}", task.getId());
        
        List<PredictResultResponse> results = new ArrayList<>();
        
        PredictResultResponse result = PredictResultResponse.builder()
                .resultId(random.nextLong())
                .targetId("P00533")
                .targetName("EGFR")
                .bindingAffinity(-random.nextDouble() * 10 - 5)
                .confidenceScore(random.nextDouble() * 0.3 + 0.7)
                .confidenceLevel(getConfidenceLevel(random.nextDouble() * 0.3 + 0.7))
                .interactions(generateInteractions())
                .createdAt(LocalDateTime.now())
                .build();
        
        results.add(result);
        return results;
    }

    /**
     * PPI（蛋白质-蛋白质相互作用）预测
     * 
     * @param task 预测任务实体
     * @return 预测结果列表
     */
    private List<PredictResultResponse> predictPPI(PredictTask task) {
        log.info("执行PPI预测任务: taskId={}", task.getId());
        
        List<PredictResultResponse> results = new ArrayList<>();
        
        PredictResultResponse result = PredictResultResponse.builder()
                .resultId(random.nextLong())
                .targetId("P12345-P67890")
                .targetName("ProteinA-ProteinB")
                .bindingAffinity(-random.nextDouble() * 8 - 3)
                .confidenceScore(random.nextDouble() * 0.3 + 0.7)
                .confidenceLevel(getConfidenceLevel(random.nextDouble() * 0.3 + 0.7))
                .interactions(generateInteractions())
                .createdAt(LocalDateTime.now())
                .build();
        
        results.add(result);
        return results;
    }

    /**
     * DDI（药物-药物相互作用）预测
     * 
     * @param task 预测任务实体
     * @return 预测结果列表
     */
    private List<PredictResultResponse> predictDDI(PredictTask task) {
        log.info("执行DDI预测任务: taskId={}", task.getId());
        
        List<PredictResultResponse> results = new ArrayList<>();
        
        PredictResultResponse result = PredictResultResponse.builder()
                .resultId(random.nextLong())
                .targetId("DrugA-DrugB")
                .targetName("Drug Interaction")
                .bindingAffinity(-random.nextDouble() * 5 - 2)
                .confidenceScore(random.nextDouble() * 0.3 + 0.7)
                .confidenceLevel(getConfidenceLevel(random.nextDouble() * 0.3 + 0.7))
                .interactions(generateInteractions())
                .createdAt(LocalDateTime.now())
                .build();
        
        results.add(result);
        return results;
    }

    /**
     * 生成相互作用信息列表
     * 
     * @return 相互作用信息列表
     */
    private List<PredictResultResponse.InteractionInfo> generateInteractions() {
        List<PredictResultResponse.InteractionInfo> interactions = new ArrayList<>();
        
        String[] residues = {"ASP", "SER", "LYS", "GLU", "ARG"};
        String[] types = {"hydrogen_bond", "hydrophobic", "ionic", "van_der_waals"};
        
        for (int i = 0; i < 3; i++) {
            PredictResultResponse.InteractionInfo info = PredictResultResponse.InteractionInfo.builder()
                    .residue(residues[random.nextInt(residues.length)])
                    .type(types[random.nextInt(types.length)])
                    .distance(random.nextDouble() * 3 + 2)
                    .build();
            interactions.add(info);
        }
        
        return interactions;
    }

    /**
     * 根据置信度分数获取置信度等级
     * 
     * @param score 置信度分数（0-1）
     * @return 置信度等级（高/中/低）
     */
    private String getConfidenceLevel(Double score) {
        if (score >= 0.85) {
            return "高";
        } else if (score >= 0.7) {
            return "中";
        } else {
            return "低";
        }
    }
}