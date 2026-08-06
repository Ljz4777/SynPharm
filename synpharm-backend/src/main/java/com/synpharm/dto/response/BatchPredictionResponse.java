package com.synpharm.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BatchPredictionResponse {

    private String status;
    private Integer total;
    private List<Map<String, Object>> results;
}
