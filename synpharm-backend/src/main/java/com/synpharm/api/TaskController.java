package com.synpharm.api;

import com.synpharm.service.TaskService;
import com.synpharm.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 任务控制器
 *
 * <p>处理预测任务相关的HTTP请求，包括查询任务列表、获取任务详情、取消任务等功能。
 * 用户身份从Spring Security上下文中获取，不需要手动传token。
 *
 * @author SynPharm Team
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务状态查询与管理接口")
public class TaskController {

    /** 任务服务，处理任务相关业务逻辑 */
    private final TaskService taskService;

    /**
     * 获取任务列表接口
     *
     * <p>查询当前登录用户的所有任务列表。
     *
     * @return 任务列表
     */
    @GetMapping
    @Operation(summary = "获取任务列表", description = "查询当前登录用户的所有任务")
    public Result<?> listTasks() {
        Long userId = getCurrentUserId();
        return Result.success(taskService.getTasksByUserId(userId));
    }

    /**
     * 获取任务详情接口
     *
     * <p>根据任务ID查询任务的详细信息，包括状态、进度等。
     *
     * @param id 任务ID
     * @return 任务详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情", description = "根据ID查询任务详细信息")
    public Result<?> getTask(@PathVariable Long id) {
        return Result.success(taskService.getTaskById(id));
    }

    /**
     * 取消任务接口
     *
     * <p>取消指定的预测任务，仅对未完成的任务有效。
     *
     * @param id 任务ID
     * @return 取消成功返回成功响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "取消任务", description = "取消指定的预测任务")
    public Result<Void> cancelTask(@PathVariable Long id) {
        taskService.cancelTask(id);
        return Result.success();
    }

    /**
     * 从Spring Security上下文中获取当前登录用户ID
     *
     * @return 当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        return (Long) authentication.getPrincipal();
    }
}
