package com.synpharm.enums;

public enum InputType {
    SMILES("smiles", "药物SMILES表达式"),
    PDB("pdb", "PDB蛋白质结构文件"),
    UNIPROT("uniprot", "UniProt蛋白质ID"),
    CSV("csv", "CSV批量文件");

    private final String code;
    private final String description;

    InputType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static InputType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("InputType code cannot be null");
        }
        for (InputType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown InputType code: " + code);
    }
}