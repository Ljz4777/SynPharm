package com.synpharm.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DDI 支持药物白名单实体。
 *
 * <p>映射数据库表 ddi_supported_drug（建表脚本 sql/10_ddi_supported_drug.sql）。
 *
 * <p><b>为什么需要这张表</b>：DDI-LLM 是<b>转导式</b>（transductive）模型，训练时只学到
 * 训练图内节点的 embedding，图外药物在数学上<b>无法预测</b>（不是精度低，而是没有向量表示）。
 * 因此"能预测哪些药物"是模型的硬能力边界，必须显式落库：
 * <ul>
 *   <li>后端据此把用户输入的 SMILES 转换成模型要求的 DrugBank ID</li>
 *   <li>转换失败时能明确告知"不在支持范围（共 N 个）"，而不是笼统报错</li>
 *   <li>权重未就绪也能提供服务（白名单是静态数据，不依赖加载模型）</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@TableName("ddi_supported_drug")
public class DdiSupportedDrug {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** DrugBank ID（DDI 模型图内节点标识，如 DB00880） */
    private String drugbankId;

    /** 药物名称 */
    private String drugName;

    /** 原始 SMILES（展示用） */
    private String smiles;

    /** 规范化 SMILES 的 SHA-256（用于反查，规则见 DdiDrugResolver） */
    private String smilesHash;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
