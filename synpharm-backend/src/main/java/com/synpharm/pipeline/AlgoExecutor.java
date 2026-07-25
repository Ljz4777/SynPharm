package com.synpharm.pipeline;

import com.synpharm.enums.AlgoType;

public interface AlgoExecutor<I, O> {
    AlgoType getAlgoType();
    O execute(I inputData);
}