package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import com.synpharm.pipeline.InputParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class SmilesInputParser implements InputParser {

    @Override
    public InputType getInputType() {
        return InputType.SMILES;
    }

    @Override
    public ParsedInput parse(String inputValue, String fileUrl) {
        log.debug("解析SMILES输入: {}", inputValue);
        
        if (inputValue == null || inputValue.trim().isEmpty()) {
            throw new IllegalArgumentException("SMILES输入不能为空");
        }
        
        String[] parts = inputValue.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("SMILES输入格式错误，需要逗号分隔的两个参数");
        }
        
        List<String> params = Arrays.asList(parts[0].trim(), parts[1].trim());
        
        return ParsedInput.builder()
                .params(params)
                .inputType(InputType.SMILES.getCode())
                .build();
    }
}