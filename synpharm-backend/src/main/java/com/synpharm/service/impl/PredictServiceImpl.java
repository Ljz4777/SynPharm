package com.synpharm.service.impl;

import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.GeneralPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.pipeline.DataPipelineFactory;
import com.synpharm.service.PredictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictServiceImpl implements PredictService {

    private final DataPipelineFactory dataPipelineFactory;

    @Override
    public PredictResultResponse predictDTI(DTIPredictRequest request, Long userId) {
        log.info("DTI预测请求: userId={}, smiles={}, targetId={}", userId, request.getSmiles(), request.getTargetId());
        
        return dataPipelineFactory.process(
                "smiles",
                "DTI",
                "json",
                request.getSmiles() + "," + request.getTargetId(),
                null
        );
    }

    @Override
    public PredictResultResponse predictPPI(PPIPredictRequest request, Long userId) {
        log.info("PPI预测请求: userId={}", userId);
        
        return dataPipelineFactory.process(
                "smiles",
                "PPI",
                "json",
                request.getProteinA() + "," + request.getProteinB(),
                null
        );
    }

    @Override
    public PredictResultResponse predictDDI(DDIPredictRequest request, Long userId) {
        log.info("DDI预测请求: userId={}", userId);
        
        return dataPipelineFactory.process(
                "smiles",
                "DDI",
                "json",
                request.getDrugA() + "," + request.getDrugB(),
                null
        );
    }

    @Override
    public PredictResultResponse predict(GeneralPredictRequest request, Long userId) {
        log.info("通用预测请求: userId={}, inputType={}, algoType={}, outputType={}", 
                userId, request.getInputType(), request.getAlgoType(), request.getOutputType());
        
        return dataPipelineFactory.process(
                request.getInputType(),
                request.getAlgoType(),
                request.getOutputType(),
                request.getInputValue(),
                request.getFileUrl()
        );
    }
}