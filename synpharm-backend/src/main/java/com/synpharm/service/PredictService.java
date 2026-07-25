package com.synpharm.service;

import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.GeneralPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.dto.response.PredictResultResponse;

public interface PredictService {

    PredictResultResponse predictDTI(DTIPredictRequest request, Long userId);

    PredictResultResponse predictPPI(PPIPredictRequest request, Long userId);

    PredictResultResponse predictDDI(DDIPredictRequest request, Long userId);

    PredictResultResponse predict(GeneralPredictRequest request, Long userId);
}