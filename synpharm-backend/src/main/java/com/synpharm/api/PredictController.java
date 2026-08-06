package com.synpharm.api;

import com.synpharm.dto.request.DTIPredictRequest;
import com.synpharm.dto.request.DDIPredictRequest;
import com.synpharm.dto.request.PPIPredictRequest;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.service.PredictService;
import com.synpharm.utils.JwtUtils;
import com.synpharm.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预测控制器
 * 
 * <p>处理药物-靶点相互作用(DTI)、蛋白质-蛋白质相互作用(PPI)、
 * 药物-药物相互作用(DDI)三种预测任务的HTTP请求。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/predict")
@RequiredArgsConstructor
@Tag(name = "预测管理", description = "DTI、PPI、DDI预测接口")
public class PredictController {

    /** 预测服务，处理预测业务逻辑 */
    private final PredictService predictService;
    
    /** JWT工具类，用于解析Token获取用户信息 */
    private final JwtUtils jwtUtils;

    /**
     * DTI预测接口
     * 
     * <p>药物-靶点相互作用预测，预测小分子药物与蛋白质靶点的结合亲和力。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param request DTI预测请求，包含SMILES分子表达式和靶点ID
     * @return 预测结果，包含结合亲和力、置信度、相互作用信息等
     */
    @PostMapping("/dti")
    @Operation(summary = "药物-靶点相互作用预测", description = "预测小分子药物与蛋白质靶点的结合亲和力")
    public Result<PredictResultResponse> predictDTI(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody DTIPredictRequest request) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        PredictResultResponse response = predictService.predictDTI(request, userId);
        return Result.success(response);
    }

    /**
     * PPI预测接口
     * 
     * <p>蛋白质-蛋白质相互作用预测，预测两个蛋白质之间的相互作用强度。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param request PPI预测请求，包含两个蛋白质的氨基酸序列
     * @return 预测结果，包含相互作用得分、置信度等
     */
    @PostMapping("/ppi")
    @Operation(summary = "蛋白质-蛋白质相互作用预测", description = "预测两个蛋白质之间的相互作用强度")
    public Result<PredictResultResponse> predictPPI(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PPIPredictRequest request) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        PredictResultResponse response = predictService.predictPPI(request, userId);
        return Result.success(response);
    }

    /**
     * DDI预测接口
     * 
     * <p>药物-药物相互作用预测，预测两种药物之间可能发生的相互作用。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param request DDI预测请求，包含两种药物的SMILES表达式
     * @return 预测结果，包含相互作用类型、风险等级等
     */
    @PostMapping("/ddi")
    @Operation(summary = "药物-药物相互作用预测", description = "预测两种药物之间可能发生的相互作用")
    public Result<PredictResultResponse> predictDDI(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody DDIPredictRequest request) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        PredictResultResponse response = predictService.predictDDI(request, userId);
        return Result.success(response);
    }

    /**
     * 预测历史查询接口
     *
     * <p>获取当前登录用户的预测历史列表（按创建时间倒序）。
     *
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @return 预测历史列表
     */
    @GetMapping("/history")
    @Operation(summary = "获取预测历史", description = "获取当前登录用户的预测历史列表")
    public Result<List<PredictResultResponse>> getHistory(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(predictService.getHistory(userId));
    }
}