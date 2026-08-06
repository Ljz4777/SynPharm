package com.synpharm.service;

import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.GeneralPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.dto.response.PredictResultResponse;

import java.util.List;

public interface PredictService {

    PredictResultResponse predictDTI(DTIPredictRequest request, Long userId);

    PredictResultResponse predictPPI(PPIPredictRequest request, Long userId);

    PredictResultResponse predictDDI(DDIPredictRequest request, Long userId);

    PredictResultResponse predict(GeneralPredictRequest request, Long userId);

    /**
     * 获取当前用户的预测历史列表
     *
     * @param userId 用户ID
     * @return 预测历史列表
     */
    List<PredictResultResponse> getHistory(Long userId);
}