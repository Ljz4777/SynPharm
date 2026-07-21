package com.synpharm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DDI预测请求DTO
 * 
 * <p>药物-药物相互作用预测请求参数。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
public class DDIPredictRequest {

    /** 药物A的SMILES表达式 */
    @NotBlank(message = "药物A SMILES不能为空")
    private String drugA;

    /** 药物B的SMILES表达式 */
    @NotBlank(message = "药物B SMILES不能为空")
    private String drugB;
}