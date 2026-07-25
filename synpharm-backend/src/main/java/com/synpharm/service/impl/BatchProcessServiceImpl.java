package com.synpharm.service.impl;

import com.synpharm.client.FastApiClient;
import com.synpharm.dto.request.PredictRequest;
import com.synpharm.dto.response.BatchPredictionResponse;
import com.synpharm.dto.response.BatchStatusResponse;
import com.synpharm.dto.response.BatchUploadResponse;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.model.entity.BatchTask;
import com.synpharm.pipeline.DataPipelineFactory;
import com.synpharm.repository.mapper.BatchTaskMapper;
import com.synpharm.service.BatchProcessService;
import com.synpharm.utils.CsvUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessServiceImpl implements BatchProcessService {

    private final BatchTaskMapper batchTaskMapper;
    private final FastApiClient fastApiClient;
    private final DataPipelineFactory dataPipelineFactory;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.result-dir:./results}")
    private String resultDir;

    private static final int CHUNK_SIZE = 50;
    private static final int PROGRESS_UPDATE_INTERVAL = 5;

    private final Map<String, BatchTaskProgress> progressCache = new ConcurrentHashMap<>();

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
            batchTaskMapper.insert(task);

            progressCache.put(batchId, new BatchTaskProgress(task));

            submitBatchTask(batchId, algoType);

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
    @Async("batchTaskExecutor")
    public void submitBatchTask(String batchId, String algoType) {
        processBatch(batchId, algoType);
    }

    @Override
    public void processBatch(String batchId, String algoType) {
        log.info("开始处理批量任务: {}", batchId);

        BatchTaskProgress progress = progressCache.get(batchId);
        if (progress == null) {
            BatchTask task = batchTaskMapper.selectByBatchId(batchId);
            if (task == null) {
                log.error("批量任务不存在: {}", batchId);
                return;
            }
            progress = new BatchTaskProgress(task);
            progressCache.put(batchId, progress);
        }

        progress.setStatus(1);
        batchTaskMapper.updateById(progress.getTask());

        List<Map<String, Object>> allResults = new ArrayList<>();
        int chunkCount = 0;

        try {
            List<String> lines = CsvUtils.readLines(progress.getTask().getFilePath());

            for (String line : lines) {
                try {
                    PredictRequest request = CsvUtils.parseLine(line, algoType);
                    if (request != null) {
                        PredictResultResponse response = dataPipelineFactory.process(
                                "smiles",
                                algoType,
                                "json",
                                line,
                                null
                        );

                        Map<String, Object> result = new HashMap<>();
                        result.put("algoType", response.getAlgoType());
                        result.put("targetId", response.getTargetId());
                        result.put("targetName", response.getTargetName());
                        result.put("bindingAffinity", response.getBindingAffinity());
                        result.put("confidenceScore", response.getConfidenceScore());
                        result.put("confidenceLevel", response.getConfidenceLevel());
                        allResults.add(result);
                        progress.addSuccess(1);
                    }
                } catch (Exception e) {
                    log.error("处理单行失败: {}", line, e);
                    progress.addFail(1);
                }

                chunkCount++;
                if (chunkCount % PROGRESS_UPDATE_INTERVAL == 0) {
                    batchTaskMapper.updateById(progress.getTask());
                }
            }

            String resultPath = resultDir + "/" + batchId + "_result.csv";
            CsvUtils.writeResultFile(resultPath, algoType, allResults);

            progress.setStatus(2);
            progress.getTask().setResultUrl("/api/batch/download/" + batchId);
            progress.setProgress(100.0);
            batchTaskMapper.updateById(progress.getTask());

            progressCache.remove(batchId);

            log.info("批量任务处理完成: {}", batchId);

        } catch (Exception e) {
            log.error("批量任务处理失败: {}", batchId, e);
            progress.setStatus(3);
            progress.getTask().setErrorMsg(e.getMessage());
            batchTaskMapper.updateById(progress.getTask());
            progressCache.remove(batchId);
        }
    }

    @Override
    public BatchStatusResponse getBatchStatus(String batchId) {
        BatchTaskProgress progress = progressCache.get(batchId);
        BatchTask task;

        if (progress != null) {
            task = progress.getTask();
        } else {
            task = batchTaskMapper.selectByBatchId(batchId);
            if (task == null) {
                throw new BusinessException("批次任务不存在");
            }
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
    public ResponseEntity<Resource> downloadBatch(String batchId) {
        BatchTask task = batchTaskMapper.selectByBatchId(batchId);
        if (task == null) {
            throw new BusinessException("批次任务不存在");
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

    private static class BatchTaskProgress {
        private final BatchTask task;

        public BatchTaskProgress(BatchTask task) {
            this.task = task;
        }

        public BatchTask getTask() {
            return task;
        }

        public void setStatus(int status) {
            task.setStatus(status);
        }

        public void addSuccess(int count) {
            task.setSuccessCount(task.getSuccessCount() + count);
            updateProgress();
        }

        public void addFail(int count) {
            task.setFailCount(task.getFailCount() + count);
            updateProgress();
        }

        public void setProgress(double progress) {
            task.setProgress(BigDecimal.valueOf(progress));
        }

        private void updateProgress() {
            int processed = task.getSuccessCount() + task.getFailCount();
            double progress = (processed * 100.0) / task.getTotalCount();
            task.setProgress(BigDecimal.valueOf(Math.min(progress, 99.99)));
        }
    }
}