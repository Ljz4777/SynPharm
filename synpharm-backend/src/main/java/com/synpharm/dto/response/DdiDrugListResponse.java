package com.synpharm.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

/**
 * DDI 可预测药物白名单响应。
 *
 * <p>DDI-LLM 是转导式（transductive）模型：训练时只学到训练图内节点的 embedding，
 * 图外新药在数学上无法预测。该列表由算法引擎的 {@code GET /v1/ddi/drugs} 提供，
 * 供前端渲染"可预测药物"下拉、或对用户输入做前置校验，
 * 避免用户输入图外药物后只收到 DRUG_NOT_SUPPORTED 而不知所以。
 *
 * <p>这里用 {@link JsonAlias} 而不是全局 snake_case 命名策略：
 * 入参兼容算法引擎的 snake_case，出参保持前端约定的 camelCase。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
public class DdiDrugListResponse {

    /** 可预测药物总数；为 0 表示 DDI 权重未就绪（此时 DDI 预测会返回 404） */
    private Integer total;

    /** 药物列表 */
    private List<DdiDrugItem> drugs;

    @Data
    public static class DdiDrugItem {

        /** DrugBank ID，如 DB00880 */
        @JsonAlias("drug_id")
        private String drugId;

        /** 药物名称，可能为 null（药名表缺失该条目时） */
        @JsonAlias("drug_name")
        private String drugName;
    }
}
