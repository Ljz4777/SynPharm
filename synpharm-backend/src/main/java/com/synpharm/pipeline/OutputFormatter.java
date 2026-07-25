package com.synpharm.pipeline;

import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.enums.OutputType;

import java.util.List;

public interface OutputFormatter {
    OutputType getOutputType();
    PredictResultResponse format(AlgoResponse resultData);
    List<PredictResultResponse> batchFormat(List<AlgoResponse> resultDataList);
}