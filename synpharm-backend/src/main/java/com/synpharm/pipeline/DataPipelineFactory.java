package com.synpharm.pipeline;

import com.synpharm.dto.request.PredictRequest;
import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.enums.AlgoType;
import com.synpharm.enums.InputType;
import com.synpharm.enums.OutputType;
import com.synpharm.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataPipelineFactory {

    private final Map<InputType, InputParser<PredictRequest>> parserMap = new HashMap<>();
    private final Map<AlgoType, AlgoExecutor<PredictRequest, AlgoResponse>> executorMap = new HashMap<>();
    private final Map<OutputType, OutputFormatter<AlgoResponse, PredictResultResponse>> formatterMap = new HashMap<>();

    public DataPipelineFactory(
            List<InputParser<PredictRequest>> parsers,
            List<AlgoExecutor<PredictRequest, AlgoResponse>> executors,
            List<OutputFormatter<AlgoResponse, PredictResultResponse>> formatters) {
        
        for (InputParser<PredictRequest> parser : parsers) {
            parserMap.put(parser.getInputType(), parser);
            log.info("注册输入解析器: {}", parser.getInputType().getCode());
        }
        
        for (AlgoExecutor<PredictRequest, AlgoResponse> executor : executors) {
            executorMap.put(executor.getAlgoType(), executor);
            log.info("注册算法执行器: {}", executor.getAlgoType().getCode());
        }
        
        for (OutputFormatter<AlgoResponse, PredictResultResponse> formatter : formatters) {
            formatterMap.put(formatter.getOutputType(), formatter);
            log.info("注册输出格式化器: {}", formatter.getOutputType().getCode());
        }
        
        log.info("数据流管道工厂初始化完成: {}个解析器, {}个执行器, {}个格式化器", 
                parserMap.size(), executorMap.size(), formatterMap.size());
    }

    public PredictResultResponse process(String inputType, String algoType, String outputType,
                                         String inputValue, String fileUrl) {
        return process(
                InputType.fromCode(inputType),
                AlgoType.fromCode(algoType),
                OutputType.fromCode(outputType),
                inputValue,
                fileUrl
        );
    }

    public PredictResultResponse process(InputType inputType, AlgoType algoType, OutputType outputType,
                                         String inputValue, String fileUrl) {
        log.info("执行数据流: {}:{}:{}", inputType.getCode(), algoType.getCode(), outputType.getCode());
        
        InputParser<PredictRequest> parser = parserMap.get(inputType);
        if (parser == null) {
            throw new BusinessException("不支持的输入类型: " + inputType.getCode());
        }
        
        AlgoExecutor<PredictRequest, AlgoResponse> executor = executorMap.get(algoType);
        if (executor == null) {
            throw new BusinessException("不支持的算法类型: " + algoType.getCode());
        }
        
        OutputFormatter<AlgoResponse, PredictResultResponse> formatter = formatterMap.get(outputType);
        if (formatter == null) {
            throw new BusinessException("不支持的输出类型: " + outputType.getCode());
        }
        
        PredictRequest parsedInput = parser.parse(inputValue, fileUrl, algoType);
        
        AlgoResponse algoResult = executor.execute(parsedInput);
        
        PredictResultResponse formattedOutput = formatter.format(algoResult);
        
        log.info("数据流执行完成");
        return formattedOutput;
    }

    public boolean supports(InputType inputType, AlgoType algoType, OutputType outputType) {
        return parserMap.containsKey(inputType) 
                && executorMap.containsKey(algoType) 
                && formatterMap.containsKey(outputType);
    }

    public boolean supports(String inputType, String algoType, String outputType) {
        try {
            return supports(
                    InputType.fromCode(inputType),
                    AlgoType.fromCode(algoType),
                    OutputType.fromCode(outputType)
            );
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}