package com.synpharm.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * FastAPI 批量预测响应。
 *
 * <p>与算法引擎的 {@code POST /v1/predict/batch} 返回值对齐：
 * status 为 success / partial / error，success 与 failed 是逐条统计的条数。
 * 此前缺少 success / failed，引擎返回的统计会被 Jackson 静默丢弃。
 */
@Data
public class BatchPredictionResponse {

    private String status;
    private Integer total;
    private Integer success;
    private Integer failed;
    private List<Map<String, Object>> results;
}
