package com.synpharm.enums;

public enum AlgoType {
    DTI("DTI", "药物-靶点相互作用"),
    PPI("PPI", "蛋白质-蛋白质相互作用"),
    DDI("DDI", "药物-药物相互作用");

    private final String code;
    private final String description;

    AlgoType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AlgoType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("AlgoType code cannot be null");
        }
        for (AlgoType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AlgoType code: " + code);
    }
}