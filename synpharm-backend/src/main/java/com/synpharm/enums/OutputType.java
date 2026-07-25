package com.synpharm.enums;

public enum OutputType {
    JSON("json", "JSON格式"),
    CSV("csv", "CSV格式"),
    VISUAL("visual", "可视化数据");

    private final String code;
    private final String description;

    OutputType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OutputType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("OutputType code cannot be null");
        }
        for (OutputType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown OutputType code: " + code);
    }
}