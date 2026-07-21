package com.synpharm.service;

import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.dto.response.PredictResultResponse;

/**
 * 预测服务接口
 */
public interface PredictService {

    PredictResultResponse predictDTI(DTIPredictRequest request, Long userId);

    PredictResultResponse predictPPI(PPIPredictRequest request, Long userId);

    PredictResultResponse predictDDI(DDIPredictRequest request, Long userId);
}