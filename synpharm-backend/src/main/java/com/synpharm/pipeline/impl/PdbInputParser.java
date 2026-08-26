package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import com.synpharm.pipeline.InputParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * PDB 输入解析器
 *
 * <p>输入格式：配体SMILES,PDB ID（逗号分隔），例如：CCO,1ABC
 * 若提供 fileUrl，则用文件URL替代PDB ID作为靶点标识。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class PdbInputParser implements InputParser {

    @Override
    public InputType getInputType() {
        return InputType.PDB;
    }

    @Override
    public ParsedInput parse(String inputValue, String fileUrl) {
        log.debug("解析PDB输入: inputValue={}, fileUrl={}", inputValue, fileUrl);

        if (inputValue == null || inputValue.trim().isEmpty()) {
            throw new IllegalArgumentException("PDB输入不能为空");
        }

        String[] parts = inputValue.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("PDB输入格式错误，需要逗号分隔的配体SMILES和PDB ID");
        }

        String ligand = parts[0].trim();
        String pdbId = parts[1].trim();

        String target = StringUtils.hasText(fileUrl) ? fileUrl : pdbId;
        List<String> params = Arrays.asList(ligand, target);

        return ParsedInput.builder()
                .params(params)
                .inputType(InputType.PDB.getCode())
                .build();
    }
}
