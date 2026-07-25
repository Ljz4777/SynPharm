package com.synpharm.pipeline.impl;

import com.synpharm.dto.request.PredictRequest;
import com.synpharm.enums.AlgoType;
import com.synpharm.enums.InputType;
import com.synpharm.pipeline.InputParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmilesInputParser implements InputParser<PredictRequest> {

    @Override
    public InputType getInputType() {
        return InputType.SMILES;
    }

    @Override
    public PredictRequest parse(String inputValue, String fileUrl, AlgoType algoType) {
        log.debug("解析SMILES输入: {}, algoType={}", inputValue, algoType.getCode());
        
        if (inputValue == null || inputValue.trim().isEmpty()) {
            throw new IllegalArgumentException("SMILES输入不能为空");
        }
        
        String[] parts = inputValue.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("SMILES输入格式错误，需要逗号分隔的两个参数");
        }
        
        String param1 = parts[0].trim();
        String param2 = parts[1].trim();
        
        return switch (algoType) {
            case DTI -> PredictRequest.forDTI(param1, param2);
            case PPI -> PredictRequest.forPPI(param1, param2);
            case DDI -> PredictRequest.forDDI(param1, param2);
        };
    }
}