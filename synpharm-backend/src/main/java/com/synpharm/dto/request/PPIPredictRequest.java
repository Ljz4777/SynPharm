package com.synpharm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * PPI预测请求DTO
 * 
 * <p>蛋白质-蛋白质相互作用预测请求参数。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
public class PPIPredictRequest {

    /** 蛋白质A的氨基酸序列 */
    @NotBlank(message = "蛋白质A序列不能为空")
    private String proteinA;

    /** 蛋白质B的氨基酸序列 */
    @NotBlank(message = "蛋白质B序列不能为空")
    private String proteinB;
}