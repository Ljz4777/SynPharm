package com.synpharm.service.impl;

import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.PredictTask;
import com.synpharm.repository.mapper.PredictTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private PredictTaskMapper taskMapper;

    private TaskServiceImpl service;

    private PredictTask ownTask;
    private PredictTask otherTask;

    @BeforeEach
    void setUp() {
        service = new TaskServiceImpl(taskMapper);

        ownTask = new PredictTask();
        ownTask.setId(1L);
        ownTask.setUserId(1L);
        ownTask.setStatus("running");

        otherTask = new PredictTask();
        otherTask.setId(2L);
        otherTask.setUserId(2L);
        otherTask.setStatus("running");
    }

    @Test
    void 查询本人任务_成功() {
        when(taskMapper.selectById(1L)).thenReturn(ownTask);

        PredictTask task = service.getTaskById(1L, 1L);

        assertNotNull(task);
        assertEquals(1L, task.getId());
    }

    @Test
    void 查询他人任务_抛FORBIDDEN_B03() {
        when(taskMapper.selectById(2L)).thenReturn(otherTask);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getTaskById(2L, 1L));
        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    void 查询任务_userId为空_抛UNAUTHORIZED_B03() {
        when(taskMapper.selectById(1L)).thenReturn(ownTask);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getTaskById(1L, null));
        assertEquals(ErrorCode.UNAUTHORIZED, e.getErrorCode());
    }

    @Test
    void 查询不存在任务_抛TASK_NOT_FOUND() {
        when(taskMapper.selectById(99L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getTaskById(99L, 1L));
        assertEquals(ErrorCode.TASK_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void 取消他人任务_抛FORBIDDEN_B03() {
        when(taskMapper.selectById(2L)).thenReturn(otherTask);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.cancelTask(2L, 1L));
        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
        verify(taskMapper, never()).updateById(any());
    }

    @Test
    void 取消本人运行中任务_置为cancelled() {
        when(taskMapper.selectById(1L)).thenReturn(ownTask);

        service.cancelTask(1L, 1L);

        assertEquals("cancelled", ownTask.getStatus());
        verify(taskMapper).updateById(ownTask);
    }

    @Test
    void 删除他人任务_抛FORBIDDEN_B03() {
        when(taskMapper.selectById(2L)).thenReturn(otherTask);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.deleteTask(2L, 1L));
        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
        verify(taskMapper, never()).deleteById(anyLong());
    }

    @Test
    void 删除本人任务_成功() {
        when(taskMapper.selectById(1L)).thenReturn(ownTask);

        service.deleteTask(1L, 1L);

        verify(taskMapper).deleteById(1L);
    }
}
