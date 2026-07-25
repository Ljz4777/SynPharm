package com.synpharm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralPredictRequest {

    @NotBlank(message = "输入类型不能为空")
    private String inputType;

    @NotBlank(message = "算法类型不能为空")
    private String algoType;

    @Builder.Default
    private String outputType = "json";

    private String inputValue;

    private String fileUrl;
}