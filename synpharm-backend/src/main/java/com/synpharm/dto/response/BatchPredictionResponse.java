package com.synpharm.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BatchPredictionResponse {

    private String status;
    private Integer total;
    private List<Map<String, Object>> results;
}
