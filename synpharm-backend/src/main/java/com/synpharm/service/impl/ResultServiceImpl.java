package com.synpharm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.PredictResult;
import com.synpharm.repository.mapper.PredictResultMapper;
import com.synpharm.service.ResultService;
import com.synpharm.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果服务实现
 *
 * <p>基于 predict_result 表实现结果列表 / 详情 / 删除，
 * 与单条预测落库（PredictServiceImpl）共用同一数据源。
 *
 * @author SynPharm Team
 * @version 1.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final PredictResultMapper resultMapper;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public Object listResults(String token, Integer page, Integer pageSize) {
        Long userId = getUserId(token);
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        // 查询当前用户全部结果（按ID倒序），内存分页（项目未配置 MyBatis-Plus 分页插件）
        List<PredictResult> all = resultMapper.selectList(
                new LambdaQueryWrapper<PredictResult>()
                        .eq(PredictResult::getUserId, userId)
                        .orderByDesc(PredictResult::getId)
        );

        int total = all.size();
        int from = Math.min((safePage - 1) * safeSize, total);
        int to = Math.min(total, from + safeSize);
        List<PredictResult> sub = from < total ? all.subList(from, to) : List.of();

        List<PredictResultResponse> list = new ArrayList<>();
        for (PredictResult entity : sub) {
            list.add(convertToResponse(entity));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        return result;
    }

    @Override
    public PredictResultResponse getResult(String token, Long id) {
        Long userId = getUserId(token);
        PredictResult entity = resultMapper.selectOne(
                new LambdaQueryWrapper<PredictResult>()
                        .eq(PredictResult::getId, id)
                        .eq(PredictResult::getUserId, userId)
        );
        if (entity == null) {
            throw new BusinessException(ErrorCode.RESULT_NOT_FOUND);
        }
        return convertToResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResult(String token, Long id) {
        Long userId = getUserId(token);
        PredictResult entity = resultMapper.selectOne(
                new LambdaQueryWrapper<PredictResult>()
                        .eq(PredictResult::getId, id)
                        .eq(PredictResult::getUserId, userId)
        );
        if (entity == null) {
            throw new BusinessException(ErrorCode.RESULT_NOT_FOUND);
        }
        resultMapper.deleteById(id);
        log.info("删除预测结果: resultId={}, userId={}", id, userId);
    }

    private Long getUserId(String token) {
        String actual = token;
        if (token != null && token.startsWith("Bearer ")) {
            actual = token.substring(7);
        }
        return jwtUtils.getUserIdFromToken(actual);
    }

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

    private List<PredictResultResponse.InteractionInfo> parseInteractions(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<PredictResultResponse.InteractionInfo>>() {
            });
        } catch (Exception e) {
            log.warn("相互作用JSON解析失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}