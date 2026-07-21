package com.synpharm.model.enums;

/**
 * 置信等级枚举
 */
public enum ConfidenceLevel {
    HIGH("high", "高置信度"),
    MEDIUM("medium", "中置信度"),
    LOW("low", "低置信度");

    private final String code;
    private final String description;

    ConfidenceLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ConfidenceLevel fromScore(double score) {
        if (score >= 0.7) {
            return HIGH;
        } else if (score >= 0.4) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}