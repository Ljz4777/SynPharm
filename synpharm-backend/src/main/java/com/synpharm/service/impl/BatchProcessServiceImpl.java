package com.synpharm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpharm.dto.response.BatchStatusResponse;
import com.synpharm.dto.response.BatchUploadResponse;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.model.entity.BatchTask;
import com.synpharm.mq.BatchTaskProducer;
import com.synpharm.pipeline.PipelineFactory;
import com.synpharm.repository.mapper.BatchTaskMapper;
import com.synpharm.service.BatchProcessService;
import com.synpharm.utils.CsvUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessServiceImpl implements BatchProcessService {

    private final BatchTaskMapper batchTaskMapper;
    private final PipelineFactory pipelineFactory;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final BatchTaskProducer batchTaskProducer;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.result-dir:./results}")
    private String resultDir;

    private static final int CHUNK_SIZE = 50;
    private static final int PROGRESS_UPDATE_INTERVAL = 5;
    private static final String PROGRESS_KEY = "batch:progress:";
    private static final int PROGRESS_EXPIRE_HOURS = 24;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchUploadResponse uploadBatch(MultipartFile file, String algoType, Long userId) {
        String batchId = UUID.randomUUID().toString();
        String fileName = batchId + "_" + file.getOriginalFilename();
        String filePath = uploadDir + "/" + fileName;

        try {
            File uploadFile = new File(filePath);
            uploadFile.getParentFile().mkdirs();
            Files.copy(file.getInputStream(), uploadFile.toPath());

            int totalCount = CsvUtils.countRows(filePath);

            BatchTask task = new BatchTask();
            task.setBatchId(batchId);
            task.setUserId(userId);
            task.setFilePath(filePath);
            task.setTotalCount(totalCount);
            task.setSuccessCount(0);
            task.setFailCount(0);
            task.setProgress(BigDecimal.ZERO);
            task.setStatus(0);
            task.setAlgoType(algoType);
            batchTaskMapper.insert(task);

            saveProgress(batchId, task);

            // 事务提交后再发送消息，避免消费者在事务未提交时读到不到记录（batch_task 为权威）
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    batchTaskProducer.sendBatchTask(batchId, algoType);
                }
            });

            return BatchUploadResponse.builder()
                    .batchId(batchId)
                    .totalCount(totalCount)
                    .status("PENDING")
                    .build();

        } catch (Exception e) {
            log.error("批量上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public void processBatch(String batchId, String algoType) {
        log.info("开始处理批量任务: {}", batchId);

        // 以 DB 为权威（Redis 仅缓存），消息消费时从 DB 读取
        BatchTask task = batchTaskMapper.selectByBatchId(batchId);
        if (task == null) {
            log.error("批量任务不存在: {}", batchId);
            return;
        }

        // 幂等：已在处理中/已完成则跳过（防重复消费/重复投递）
        if (task.getStatus() != null && (task.getStatus() == 1 || task.getStatus() == 2)) {
            log.info("批量任务已在处理或已完成, 跳过: batchId={}, status={}", batchId, task.getStatus());
            return;
        }

        if (algoType != null && !algoType.isBlank()) {
            task.setAlgoType(algoType);
        }

        task.setStatus(1);
        batchTaskMapper.updateById(task);
        saveProgress(batchId, task);

        List<Map<String, Object>> allResults = new ArrayList<>();
        int chunkCount = 0;

        try {
            List<String> lines = CsvUtils.readLines(task.getFilePath());

            List<String> chunk = new ArrayList<>();
            for (String line : lines) {
                chunk.add(line);

                if (chunk.size() >= CHUNK_SIZE) {
                    List<PredictResultResponse> responses = pipelineFactory.batchProcess(
                            "smiles", algoType, "json", chunk
                    );

                    for (PredictResultResponse response : responses) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("algoType", response.getAlgoType());
                        result.put("targetId", response.getTargetId());
                        result.put("targetName", response.getTargetName());
                        result.put("bindingAffinity", response.getBindingAffinity());
                        result.put("confidenceScore", response.getConfidenceScore());
                        result.put("confidenceLevel", response.getConfidenceLevel());
                        allResults.add(result);
                    }

                    task.setSuccessCount(task.getSuccessCount() + chunk.size());
                    chunk.clear();
                    chunkCount++;

                    if (chunkCount % PROGRESS_UPDATE_INTERVAL == 0) {
                        batchTaskMapper.updateById(task);
                    }
                    saveProgress(batchId, task);
                }
            }

            if (!chunk.isEmpty()) {
                List<PredictResultResponse> responses = pipelineFactory.batchProcess(
                        "smiles", algoType, "json", chunk
                );

                for (PredictResultResponse response : responses) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("algoType", response.getAlgoType());
                    result.put("targetId", response.getTargetId());
                    result.put("targetName", response.getTargetName());
                    result.put("bindingAffinity", response.getBindingAffinity());
                    result.put("confidenceScore", response.getConfidenceScore());
                    result.put("confidenceLevel", response.getConfidenceLevel());
                    allResults.add(result);
                }

                task.setSuccessCount(task.getSuccessCount() + chunk.size());
                saveProgress(batchId, task);
            }

            String resultPath = resultDir + "/" + batchId + "_result.csv";
            CsvUtils.writeResultFile(resultPath, algoType, allResults);

            task.setStatus(2);
            task.setResultUrl("/api/batch/download/" + batchId);
            task.setProgress(BigDecimal.valueOf(100));
            batchTaskMapper.updateById(task);

            redisTemplate.delete(PROGRESS_KEY + batchId);

            log.info("批量任务处理完成: {}", batchId);

        } catch (Exception e) {
            log.error("批量任务处理失败: {}", batchId, e);
            task.setStatus(3);
            task.setErrorMsg(e.getMessage());
            batchTaskMapper.updateById(task);
            saveProgress(batchId, task);
        }
    }

    @Override
    public BatchStatusResponse getBatchStatus(String batchId, Long userId) {
        BatchTask task = batchTaskMapper.selectByBatchId(batchId);
        if (task == null) {
            throw new BusinessException("批次任务不存在");
        }

        // 归属校验：仅本人可查看（修复 IDOR）
        if (userId != null && !userId.equals(task.getUserId())) {
            throw new BusinessException("无权访问该批次任务");
        }

        String statusText = switch (task.getStatus()) {
            case 0 -> "PENDING";
            case 1 -> "PROCESSING";
            case 2 -> "SUCCESS";
            case 3 -> "FAIL";
            default -> "UNKNOWN";
        };

        return BatchStatusResponse.builder()
                .batchId(task.getBatchId())
                .algoType(task.getAlgoType())
                .totalCount(task.getTotalCount())
                .successCount(task.getSuccessCount())
                .failCount(task.getFailCount())
                .progress(task.getProgress())
                .status(statusText)
                .resultUrl(task.getResultUrl())
                .createTime(task.getCreateTime())
                .updateTime(task.getUpdateTime())
                .build();
    }

    @Override
    public ResponseEntity<Resource> downloadBatch(String batchId, Long userId) {
        BatchTask task = batchTaskMapper.selectByBatchId(batchId);
        if (task == null) {
            throw new BusinessException("批次任务不存在");
        }

        // 归属校验：仅本人可下载（修复 IDOR）
        if (userId != null && !userId.equals(task.getUserId())) {
            throw new BusinessException("无权访问该批次任务");
        }

        String resultPath = resultDir + "/" + batchId + "_result.csv";
        File file = new File(resultPath);

        if (!file.exists()) {
            throw new BusinessException("结果文件不存在");
        }

        Resource resource = new FileSystemResource(file);
        String filename = batchId + "_result.csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    private void saveProgress(String batchId, BatchTask task) {
        try {
            String json = objectMapper.writeValueAsString(task);
            redisTemplate.opsForValue().set(PROGRESS_KEY + batchId, json, PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("保存批量任务进度失败: {}", batchId, e);
        }
    }

    private BatchTask getProgress(String batchId) {
        try {
            String json = redisTemplate.opsForValue().get(PROGRESS_KEY + batchId);
            if (json != null) {
                return objectMapper.readValue(json, BatchTask.class);
            }
        } catch (JsonProcessingException e) {
            log.error("获取批量任务进度失败: {}", batchId, e);
        }
        return null;
    }
}