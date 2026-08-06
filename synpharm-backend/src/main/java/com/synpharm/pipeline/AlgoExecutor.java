package com.synpharm.pipeline;

import com.synpharm.dto.ParsedInput;
import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.enums.AlgoType;

import java.util.List;

public interface AlgoExecutor {
    AlgoType getAlgoType();
    AlgoResponse execute(ParsedInput inputData);
    List<AlgoResponse> batchExecute(List<ParsedInput> inputDataList);
}