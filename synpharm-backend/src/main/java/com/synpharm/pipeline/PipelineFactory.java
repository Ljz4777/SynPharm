package com.synpharm.pipeline;

import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.enums.AlgoType;
import com.synpharm.enums.InputType;
import com.synpharm.enums.OutputType;

import java.util.List;

public interface PipelineFactory {
    
    PredictResultResponse process(InputType inputType, AlgoType algoType, OutputType outputType,
                                  String inputValue, String fileUrl);
    
    PredictResultResponse process(String inputType, String algoType, String outputType,
                                  String inputValue, String fileUrl);
    
    List<PredictResultResponse> batchProcess(InputType inputType, AlgoType algoType, OutputType outputType,
                                             List<String> inputs);
    
    List<PredictResultResponse> batchProcess(String inputType, String algoType, String outputType,
                                             List<String> inputs);
    
    boolean supports(InputType inputType, AlgoType algoType, OutputType outputType);
    
    boolean supports(String inputType, String algoType, String outputType);
}