package com.synpharm.service;

import com.synpharm.dto.response.BatchStatusResponse;
import com.synpharm.dto.response.BatchUploadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface BatchProcessService {

    BatchUploadResponse uploadBatch(MultipartFile file, String algoType, Long userId);

    void submitBatchTask(String batchId, String algoType);

    void processBatch(String batchId, String algoType);

    BatchStatusResponse getBatchStatus(String batchId);

    ResponseEntity<org.springframework.core.io.Resource> downloadBatch(String batchId);
}
