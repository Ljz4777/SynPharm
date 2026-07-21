package com.synpharm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTI预测请求DTO
 * 
 * <p>药物-靶点相互作用预测请求参数。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
public class DTIPredictRequest {

    /** 药物分子的SMILES表达式 */
    @NotBlank(message = "SMILES不能为空")
    private String smiles;

    /** 靶点蛋白ID */
    @NotBlank(message = "靶点ID不能为空")
    private String targetId;
}