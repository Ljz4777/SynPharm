package com.synpharm.api;

import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.service.ResultService;
import com.synpharm.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 结果控制器
 * 
 * <p>处理预测结果相关的HTTP请求，包括查询结果列表、获取结果详情、删除结果等功能。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
@Tag(name = "结果管理", description = "预测结果查询接口")
public class ResultController {

    /** 结果服务，处理结果相关业务逻辑 */
    private final ResultService resultService;

    /**
     * 获取结果列表接口
     * 
     * <p>分页查询当前用户的预测结果列表。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param page 页码，默认值为1
     * @param pageSize 每页大小，默认值为10
     * @return 分页结果列表
     */
    @GetMapping
    @Operation(summary = "获取结果列表", description = "分页查询当前用户的预测结果")
    public Result<?> listResults(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(resultService.listResults(token, page, pageSize));
    }

    /**
     * 获取结果详情接口
     * 
     * <p>根据结果ID查询单个预测结果的详细信息。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param id 结果ID
     * @return 预测结果详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取结果详情", description = "根据ID查询预测结果详细信息")
    public Result<PredictResultResponse> getResult(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        return Result.success(resultService.getResult(token, id));
    }

    /**
     * 删除结果接口
     * 
     * <p>根据结果ID删除指定的预测结果。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param id 结果ID
     * @return 删除成功返回成功响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除结果", description = "删除指定的预测结果")
    public Result<Void> deleteResult(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        resultService.deleteResult(token, id);
        return Result.success();
    }
}