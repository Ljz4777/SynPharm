package com.synpharm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpharm.client.FastApiClient;
import com.synpharm.dto.request.GeneralPredictRequest;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.PredictResult;
import com.synpharm.pipeline.PipelineFactory;
import com.synpharm.repository.mapper.PredictResultMapper;
import com.synpharm.repository.mapper.PredictTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictServiceImplTest {

    @Mock
    private PipelineFactory pipelineFactory;
    @Mock
    private PredictTaskMapper taskMapper;
    @Mock
    private PredictResultMapper resultMapper;
    @Mock
    private FastApiClient fastApiClient;

    private PredictServiceImpl service;

    private GeneralPredictRequest request;

    @BeforeEach
    void setUp() {
        service = new PredictServiceImpl(pipelineFactory, taskMapper, resultMapper,
                new ObjectMapper(), fastApiClient);

        request = GeneralPredictRequest.builder()
                .inputType("smiles")
                .algoType("DTI")
                .outputType("json")
                .inputValue("CCO,P00533")
                .build();
    }

    private PredictResultResponse response() {
        return PredictResultResponse.builder()
                .algoType("DTI")
                .targetId("T1")
                .confidenceScore(0.9)
                .build();
    }

    @Test
    void 相同输入重复提交_幂等复用已有结果_C07() {
        when(pipelineFactory.process(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(response());

        PredictResult existing = new PredictResult();
        existing.setId(5L);
        existing.setCreatedAt(LocalDateTime.of(2026, 9, 1, 10, 0));
        when(resultMapper.selectOne(any())).thenReturn(existing);

        PredictResultResponse result = service.predict(request, 1L);

        assertEquals(5L, result.getId());
        // 幂等命中：不再建任务、不再插结果
        verify(taskMapper, never()).insert(any());
        verify(resultMapper, never()).insert(any());
    }

    @Test
    void 相同输入但用户不同_指纹不同_正常落库_C07() {
        when(pipelineFactory.process(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(response());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(taskMapper.insert(any())).thenReturn(1);
        when(resultMapper.insert(any())).thenReturn(1);

        service.predict(request, 1L);
        service.predict(request, 2L);

        verify(resultMapper, times(2)).insert(any(PredictResult.class));
    }

    @Test
    void 落库失败_抛SYSTEM_ERROR不再静默_C07() {
        when(pipelineFactory.process(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(response());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(taskMapper.insert(any())).thenThrow(new RuntimeException("DB down"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.predict(request, 1L));
        assertEquals(ErrorCode.SYSTEM_ERROR, e.getErrorCode());
    }

    @Test
    void 插入结果带指纹_C07() {
        when(pipelineFactory.process(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(response());
        when(resultMapper.selectOne(any())).thenReturn(null);
        when(taskMapper.insert(any())).thenReturn(1);
        when(resultMapper.insert(any())).thenReturn(1);

        service.predict(request, 1L);

        verify(resultMapper).insert(argThat(r -> r.getFingerprint() != null
                && r.getFingerprint().length() == 64));
    }
}
