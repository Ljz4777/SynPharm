package com.synpharm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DDI预测请求DTO
 *
 * <p>药物-药物相互作用预测请求参数。
 * D-01：字段命名为 drugAInput/drugBInput——实际输入可以是 SMILES、
 * DrugBank ID 或药物名（经 DdiDrugResolver 解析），不再是纯 SMILES。
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@Data
public class DDIPredictRequest {

    /** 药物A的输入（SMILES / DrugBank ID / 药物名） */
    @NotBlank(message = "药物A输入不能为空")
    private String drugAInput;

    /** 药物B的输入（SMILES / DrugBank ID / 药物名） */
    @NotBlank(message = "药物B输入不能为空")
    private String drugBInput;
}
