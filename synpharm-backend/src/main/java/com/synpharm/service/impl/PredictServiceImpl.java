package com.synpharm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.GeneralPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.client.FastApiClient;
import com.synpharm.dto.response.DdiDrugListResponse;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.PredictResult;
import com.synpharm.model.entity.PredictTask;
import com.synpharm.pipeline.PipelineFactory;
import com.synpharm.repository.mapper.PredictResultMapper;
import com.synpharm.repository.mapper.PredictTaskMapper;
import com.synpharm.service.PredictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictServiceImpl implements PredictService {

    private final PipelineFactory pipelineFactory;
    private final PredictTaskMapper taskMapper;
    private final PredictResultMapper resultMapper;
    private final ObjectMapper objectMapper;
    private final FastApiClient fastApiClient;

    @Override
    @Deprecated
    public PredictResultResponse predictDTI(DTIPredictRequest request, Long userId) {
        log.warn("已废弃的方法 predictDTI，请使用通用预测接口 predict()");
        return predict(GeneralPredictRequest.builder()
                .inputType("smiles")
                .algoType("DTI")
                .outputType("json")
                .inputValue(request.getSmiles() + "," + request.getTargetId())
                .build(), userId);
    }

    @Override
    @Deprecated
    public PredictResultResponse predictPPI(PPIPredictRequest request, Long userId) {
        log.warn("已废弃的方法 predictPPI，请使用通用预测接口 predict()");
        return predict(GeneralPredictRequest.builder()
                .inputType("smiles")
                .algoType("PPI")
                .outputType("json")
                .inputValue(request.getProteinA() + "," + request.getProteinB())
                .build(), userId);
    }

    @Override
    @Deprecated
    public PredictResultResponse predictDDI(DDIPredictRequest request, Long userId) {
        log.warn("已废弃的方法 predictDDI，请使用通用预测接口 predict()");
        return predict(GeneralPredictRequest.builder()
                .inputType("smiles")
                .algoType("DDI")
                .outputType("json")
                .inputValue(request.getDrugAInput() + "," + request.getDrugBInput())
                .build(), userId);
    }

    @Override
    public PredictResultResponse predict(GeneralPredictRequest request, Long userId) {
        // D-05：不在事务内做 30~60s 的外部 HTTP 调用。
        // 推理在事务外执行，落库在 savePrediction 内以短事务完成（单条 insert 自带事务）。
        log.info("通用预测请求: userId={}, inputType={}, algoType={}, outputType={}",
                userId, request.getInputType(), request.getAlgoType(), request.getOutputType());

        PredictResultResponse response = pipelineFactory.process(
                request.getInputType(),
                request.getAlgoType(),
                request.getOutputType(),
                request.getInputValue(),
                request.getFileUrl()
        );

        // 预测结果落库：创建隐式任务 + 结果记录（供历史/结果查询使用）
        savePrediction(request, userId, response);

        return response;
    }

    @Override
    public List<PredictResultResponse> getHistory(Long userId) {
        List<PredictResult> entities = resultMapper.selectList(
                new LambdaQueryWrapper<PredictResult>()
                        .eq(PredictResult::getUserId, userId)
                        .orderByDesc(PredictResult::getId)
        );
        List<PredictResultResponse> list = new ArrayList<>();
        for (PredictResult entity : entities) {
            list.add(convertToResponse(entity));
        }
        return list;
    }

    /**
     * 落库：为单条预测创建隐式任务记录 + 结果记录。
     * <p>predict_result.task_id 为 NOT NULL 外键，故先创建任务再创建结果。
     * <p>C-07：按指纹幂等——同用户、同算法、同输入在 10 分钟内重复提交直接返回已有结果；
     * 落库失败不再静默，抛出 SYSTEM_ERROR 让用户感知。
     */
    private void savePrediction(GeneralPredictRequest request, Long userId, PredictResultResponse response) {
        String algoType = response.getAlgoType() != null ? response.getAlgoType() : request.getAlgoType();
        String fingerprint = computeFingerprint(request, userId, algoType);
        try {
            // 1. 幂等检查：指纹命中直接复用已有结果（C-07）
            PredictResult existing = resultMapper.selectOne(new LambdaQueryWrapper<PredictResult>()
                    .eq(PredictResult::getFingerprint, fingerprint)
                    .orderByDesc(PredictResult::getId)
                    .last("LIMIT 1")
            );
            if (existing != null) {
                response.setId(existing.getId());
                response.setCreatedAt(existing.getCreatedAt());
                response.setLigandSmiles(existing.getLigandSmiles());
                log.info("幂等命中，复用已有预测结果: resultId={}, fingerprint={}", existing.getId(), fingerprint);
                return;
            }

            // 2. 创建隐式任务
            PredictTask task = new PredictTask();
            task.setTaskNo(generateNo("T"));
            task.setUserId(userId);
            task.setPredictType(toPredictType(algoType));
            task.setInputType(request.getInputType());
            task.setInputValue(request.getInputValue());
            task.setFileUrl(request.getFileUrl());
            task.setStatus("completed");
            task.setProgress(100);
            task.setStartedAt(LocalDateTime.now());
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.insert(task);

            // 3. 创建预测结果（带指纹）
            PredictResult entity = new PredictResult();
            entity.setResultNo(generateNo("R"));
            entity.setFingerprint(fingerprint);
            entity.setTaskId(task.getId());
            entity.setUserId(userId);
            entity.setTargetId(response.getTargetId());
            entity.setTargetName(response.getTargetName());
            entity.setLigandSmiles(extractLigandSmiles(request, algoType));
            entity.setBindingAffinity(response.getBindingAffinity());
            entity.setConfidenceScore(response.getConfidenceScore());
            entity.setConfidenceLevel(response.getConfidenceLevel());
            entity.setInteractions(writeJson(response.getInteractions()));
            entity.setPredictionData(writeJson(response));
            entity.setDatasetSource("single-predict");
            resultMapper.insert(entity);

            response.setId(entity.getId());
            response.setCreatedAt(entity.getCreatedAt());
            response.setLigandSmiles(entity.getLigandSmiles());
            log.info("预测结果落库成功: resultId={}, taskId={}", entity.getId(), task.getId());
        } catch (Exception e) {
            // C-07：落库失败不再静默吞掉，带堆栈日志 + 业务异常上抛
            log.error("预测结果落库失败: userId={}, algoType={}", userId, algoType, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "预测结果保存失败，请稍后重试");
        }
    }

    /**
     * 计算预测指纹（C-07）：sha256(userId | algoType小写 | 归一化输入)
     * <p>归一化规则：输入值去首尾空白并压缩内部连续空白，
     * 使"相同语义"的输入产生相同指纹。唯一索引 uk_fingerprint 在 DB 层兜底并发。
     */
    private String computeFingerprint(GeneralPredictRequest request, Long userId, String algoType) {
        String normalized = request.getInputValue() == null ? "" : request.getInputValue().trim().replaceAll("\\s+", "");
        String raw = userId + "|" + (algoType == null ? "" : algoType.toLowerCase()) + "|" + normalized;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    private String toPredictType(String algoType) {
        return algoType == null ? null : algoType.toLowerCase();
    }

    private String extractLigandSmiles(GeneralPredictRequest request, String algoType) {
        if (algoType != null && ("DTI".equalsIgnoreCase(algoType) || "DDI".equalsIgnoreCase(algoType))) {
            String value = request.getInputValue();
            if (value != null && value.contains(",")) {
                return value.split(",")[0].trim();
            }
            return value;
        }
        return null;
    }

    private String generateNo(String prefix) {
        return prefix + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private String writeJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("JSON序列化失败", e);
            return null;
        }
    }

    /**
     * 实体转响应DTO（反序列化 interactions / 从 predictionData 提取 algoType）。
     */
    private PredictResultResponse convertToResponse(PredictResult entity) {
        String algoType = null;
        if (entity.getPredictionData() != null) {
            try {
                JsonNode node = objectMapper.readTree(entity.getPredictionData());
                if (node.has("algoType") && !node.get("algoType").isNull()) {
                    algoType = node.get("algoType").asText();
                }
            } catch (Exception ignored) {
                // 提取失败则 algoType 为 null
            }
        }

        return PredictResultResponse.builder()
                .id(entity.getId())
                .algoType(algoType)
                .targetId(entity.getTargetId())
                .targetName(entity.getTargetName())
                .ligandSmiles(entity.getLigandSmiles())
                .bindingAffinity(entity.getBindingAffinity())
                .confidenceScore(entity.getConfidenceScore())
                .confidenceLevel(entity.getConfidenceLevel())
                .interactions(parseInteractions(entity.getInteractions()))
                .createdAt(entity.getCreatedAt())
                .datasetInfo(defaultDatasetInfo(algoType))
                .build();
    }

    private PredictResultResponse.DatasetInfo defaultDatasetInfo(String algoType) {
        return PredictResultResponse.DatasetInfo.builder()
                .name(algoType == null ? "AI预测" : algoType + "预测结果")
                .size(0)
                .description("由 FastAPI 算法引擎计算")
                .source("fastapi")
                .build();
    }

    @Override
    public DdiDrugListResponse getDdiSupportedDrugs() {
        // 白名单来自算法引擎（DDI-LLM 训练图内药物）；引擎不可用时由 FastApiClient 抛出业务异常
        return fastApiClient.fetchDdiDrugs();
    }

    private List<PredictResultResponse.InteractionInfo> parseInteractions(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<PredictResultResponse.InteractionInfo>>() {
            });
        } catch (Exception e) {
            log.warn("相互作用JSON解析失败", e);
            return new ArrayList<>();
        }
    }
}