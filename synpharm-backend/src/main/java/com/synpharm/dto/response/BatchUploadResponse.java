package com.synpharm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatchUploadResponse {

    private String batchId;
    private Integer totalCount;
    private String status;
}
