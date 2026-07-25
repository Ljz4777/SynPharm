package com.synpharm.exception;

import lombok.Getter;

@Getter
public class PipelineException extends RuntimeException {
    
    private final String stage;
    
    public PipelineException(String stage, String message) {
        super("管道[" + stage + "]执行失败: " + message);
        this.stage = stage;
    }
    
    public PipelineException(String stage, String message, Throwable cause) {
        super("管道[" + stage + "]执行失败: " + message, cause);
        this.stage = stage;
    }
    
    public static PipelineException parse(String message) {
        return new PipelineException("parse", message);
    }
    
    public static PipelineException parse(String message, Throwable cause) {
        return new PipelineException("parse", message, cause);
    }
    
    public static PipelineException execute(String message) {
        return new PipelineException("execute", message);
    }
    
    public static PipelineException execute(String message, Throwable cause) {
        return new PipelineException("execute", message, cause);
    }
    
    public static PipelineException format(String message) {
        return new PipelineException("format", message);
    }
    
    public static PipelineException format(String message, Throwable cause) {
        return new PipelineException("format", message, cause);
    }
}