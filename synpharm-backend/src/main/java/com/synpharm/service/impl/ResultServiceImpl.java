package com.synpharm.service.impl;

import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 结果服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    @Override
    public Object listResults(String token, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public PredictResultResponse getResult(String token, Long id) {
        return null;
    }

    @Override
    public void deleteResult(String token, Long id) {
    }
}