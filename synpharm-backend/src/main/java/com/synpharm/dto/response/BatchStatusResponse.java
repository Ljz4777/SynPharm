package com.synpharm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BatchStatusResponse {

    private String batchId;
    private String algoType;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private BigDecimal progress;
    private String status;
    private String resultUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
