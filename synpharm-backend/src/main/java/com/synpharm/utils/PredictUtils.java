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
        
        // ⚠️ 不再返回 Random 生成的假结果（问题总账 C-05）。
        //
        // 原先这里按 predictType 分发到 predictDTI/predictPPI/predictDDI，三者都用
        // random.nextDouble() 编造靶点、亲和力、置信度和相互作用 —— 调用方
        // （TaskServiceImpl#executeTask）拿到的是一份"看起来完全正常"的随机报告。
        // 真正的预测早已由 PipelineFactory / PredictService 走 FastAPI 算法引擎。
        //
        // 这里改成直接抛错：任何误用都会立刻暴露，而不是静默产出假数据。
        // 下面三个私有方法已不可达，留待专门的清理提交连同本类一起删除。
        throw new UnsupportedOperationException(
                "PredictUtils#predict 已弃用：它产出的是 Random 假数据，"
                        + "请改用 PipelineFactory / PredictService 调用真实算法引擎");
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
                .id(random.nextLong())
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
                .id(random.nextLong())
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
                .id(random.nextLong())
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
                    .residueName(residues[random.nextInt(residues.length)])
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