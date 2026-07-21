package com.synpharm.model.enums;

/**
 * 预测类型枚举
 */
public enum PredictType {
    PPI("ppi", "蛋白质-蛋白质相互作用预测"),
    DTI("dti", "药物-靶点相互作用预测"),
    DDI("ddi", "药物-药物相互作用预测");

    private final String code;
    private final String description;

    PredictType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PredictType fromCode(String code) {
        for (PredictType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}