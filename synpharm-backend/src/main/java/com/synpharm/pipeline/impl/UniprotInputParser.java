package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import com.synpharm.pipeline.InputParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * UniProt 输入解析器
 *
 * <p>输入格式：配体SMILES,UniProt ID（逗号分隔）
 * 例如：CCO,P12345
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class UniprotInputParser implements InputParser {

    @Override
    public InputType getInputType() {
        return InputType.UNIPROT;
    }

    @Override
    public ParsedInput parse(String inputValue, String fileUrl) {
        log.debug("解析UniProt输入: {}", inputValue);

        if (inputValue == null || inputValue.trim().isEmpty()) {
            throw new IllegalArgumentException("UniProt输入不能为空");
        }

        String[] parts = inputValue.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("UniProt输入格式错误，需要逗号分隔的配体SMILES和UniProt ID");
        }

        List<String> params = Arrays.asList(parts[0].trim(), parts[1].trim());

        return ParsedInput.builder()
                .params(params)
                .inputType(InputType.UNIPROT.getCode())
                .build();
    }
}
