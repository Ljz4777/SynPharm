package com.synpharm.service.impl;

import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.GeneralPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.pipeline.PipelineFactory;
import com.synpharm.service.PredictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictServiceImpl implements PredictService {

    private final PipelineFactory pipelineFactory;

    @Override
    @Deprecated
    public PredictResultResponse predictDTI(DTIPredictRequest request, Long userId) {
        log.warn("已废弃的方法 predictDTI，请使用通用预测接口 predict()");
        return predict(GeneralPredictRequest.builder()
                .inputType("smiles")
                .algoType("DTI")
                .outputType("json")
                .inputValue(request.getSmiles() + "," + request.getTargetId())
                .build(), userId);
    }

    @Override
    @Deprecated
    public PredictResultResponse predictPPI(PPIPredictRequest request, Long userId) {
        log.warn("已废弃的方法 predictPPI，请使用通用预测接口 predict()");
        return predict(GeneralPredictRequest.builder()
                .inputType("smiles")
                .algoType("PPI")
                .outputType("json")
                .inputValue(request.getProteinA() + "," + request.getProteinB())
                .build(), userId);
    }

    @Override
    @Deprecated
    public PredictResultResponse predictDDI(DDIPredictRequest request, Long userId) {
        log.warn("已废弃的方法 predictDDI，请使用通用预测接口 predict()");
        return predict(GeneralPredictRequest.builder()
                .inputType("smiles")
                .algoType("DDI")
                .outputType("json")
                .inputValue(request.getDrugA() + "," + request.getDrugB())
                .build(), userId);
    }

    @Override
    public PredictResultResponse predict(GeneralPredictRequest request, Long userId) {
        log.info("通用预测请求: userId={}, inputType={}, algoType={}, outputType={}", 
                userId, request.getInputType(), request.getAlgoType(), request.getOutputType());
        
        return pipelineFactory.process(
                request.getInputType(),
                request.getAlgoType(),
                request.getOutputType(),
                request.getInputValue(),
                request.getFileUrl()
        );
    }
}