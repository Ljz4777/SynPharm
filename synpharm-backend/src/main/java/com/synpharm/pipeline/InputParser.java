package com.synpharm.pipeline;

import com.synpharm.enums.AlgoType;
import com.synpharm.enums.InputType;

public interface InputParser<T> {
    InputType getInputType();
    T parse(String inputValue, String fileUrl, AlgoType algoType);
}