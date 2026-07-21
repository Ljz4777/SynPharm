package com.synpharm.service;

import com.synpharm.dto.response.PredictResultResponse;

/**
 * 结果服务接口
 */
public interface ResultService {

    Object listResults(String token, Integer page, Integer pageSize);

    PredictResultResponse getResult(String token, Long id);

    void deleteResult(String token, Long id);
}