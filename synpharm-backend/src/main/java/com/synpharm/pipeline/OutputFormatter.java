package com.synpharm.pipeline;

import com.synpharm.enums.OutputType;

public interface OutputFormatter<I, O> {
    OutputType getOutputType();
    O format(I resultData);
}