package com.synpharm.pipeline;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;

public interface InputParser {
    InputType getInputType();
    ParsedInput parse(String inputValue, String fileUrl);
}