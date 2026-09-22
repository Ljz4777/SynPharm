package com.synpharm.pipeline.resolve;

import com.synpharm.exception.PredictionErrorCode;
import com.synpharm.exception.PredictionException;
import com.synpharm.model.entity.DdiSupportedDrug;
import com.synpharm.repository.mapper.DdiSupportedDrugMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * DDI 输入转换层：把用户真实输入的 SMILES 翻译成模型要求的 DrugBank ID。
 *
 * <p><b>为什么需要它</b>：用户手里只有 SMILES（DrugBank 页面上复制的就是 SMILES），
 * 而 DDI-LLM 是转导式模型，图节点标识是 DrugBank ID。两侧语言不同，
 * 必须在"用户输入之后、进模型之前"做一次翻译 —— 而不是要求用户改输入方式。
 *
 * <p><b>匹配规则</b>（按顺序）：
 * <ol>
 *   <li>用户直接填 DrugBank ID（形如 {@code DB00880}）→ 命中白名单即通过</li>
 *   <li>用户填 SMILES → 规范化后取 SHA-256，用 {@code smiles_hash} 反查</li>
 * </ol>
 *
 * <p><b>规范化规则必须与生成脚本一致</b>：仅去除所有空白字符。
 * SMILES 大小写有意义（{@code C} 与 {@code c} 是不同原子），因此不做大小写转换。
 * 生成侧见 {@code synpharm-fastapi/models/DDI-LLM} 的清单导出脚本。
 *
 * <p><b>加载策略</b>：白名单仅约 1300 条，首次使用时全量加载进内存做反查，
 * 不做逐条查询。加载失败不缓存，下一个请求会重试（表可能刚建好）。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DdiDrugResolver {

    /** 直接填 DrugBank ID 的格式（保留原有"手填编号"用法） */
    private static final Pattern DRUGBANK_ID_PATTERN =
            Pattern.compile("^DB\\d{5}$", Pattern.CASE_INSENSITIVE);

    private final DdiSupportedDrugMapper mapper;

    /** smiles_hash -> drugbank_id；null 表示尚未成功加载 */
    private volatile Map<String, String> hashToId;

    /** 白名单内全部 DrugBank ID（大写） */
    private volatile Set<String> supportedIds;

    /**
     * 把用户输入转换成模型可用的 DrugBank ID。
     *
     * @param rawInput 用户原始输入（SMILES 或 DrugBank ID）
     * @return 白名单内的 DrugBank ID
     * @throws PredictionException 输入为空、清单不可用、或药物不在支持范围内
     */
    public String resolve(String rawInput) {
        String input = rawInput == null ? "" : rawInput.trim();
        if (input.isEmpty()) {
            throw new PredictionException(PredictionErrorCode.INVALID_SMILES, "DDI 药物输入不能为空");
        }

        ensureLoaded();
        Map<String, String> hashIndex = this.hashToId;
        Set<String> ids = this.supportedIds;
        if (hashIndex == null || ids == null) {
            throw new PredictionException(PredictionErrorCode.DRUG_NOT_SUPPORTED,
                    "DDI 支持药物清单不可用：数据库表 ddi_supported_drug 读取失败。"
                            + "请确认已执行 sql/10_ddi_supported_drug.sql");
        }

        // 1) 直接填 DrugBank ID
        if (DRUGBANK_ID_PATTERN.matcher(input).matches()) {
            String upper = input.toUpperCase();
            if (ids.contains(upper)) {
                return upper;
            }
            throw notSupported(input, ids.size(), "该 DrugBank ID 不在 DDI 支持范围内");
        }

        // 2) 按 SMILES 反查
        String drugbankId = hashIndex.get(sha256(normalize(input)));
        if (drugbankId != null) {
            return drugbankId;
        }
        throw notSupported(input, ids.size(), "该 SMILES 未匹配到 DDI 支持的药物");
    }

    /** 白名单规模；清单不可用或尚未加载时返回 0。 */
    public int supportedCount() {
        ensureLoaded();
        Set<String> ids = this.supportedIds;
        return ids == null ? 0 : ids.size();
    }

    private PredictionException notSupported(String input, int count, String reason) {
        String shortInput = input.length() > 60 ? input.substring(0, 60) + "..." : input;
        return new PredictionException(PredictionErrorCode.DRUG_NOT_SUPPORTED,
                reason + "：" + shortInput
                        + "。DDI-LLM 是转导式模型，只能预测训练图内 " + count
                        + " 个药物之间的相互作用；请确认是从 DrugBank 完整复制的 SMILES，"
                        + "或改用支持清单内的药物");
    }

    private void ensureLoaded() {
        if (hashToId != null) {
            return;
        }
        synchronized (this) {
            if (hashToId == null) {
                load();
            }
        }
    }

    private void load() {
        List<DdiSupportedDrug> all;
        try {
            all = mapper.selectList(null);
        } catch (Exception e) {
            // 表不存在或数据库不可用：故意不缓存，下一个请求重试
            log.error("读取 DDI 支持药物清单失败（是否已执行 sql/10_ddi_supported_drug.sql？）: {}",
                    e.getMessage());
            return;
        }

        Map<String, String> hashIndex = new HashMap<>(all.size() * 2);
        Set<String> ids = new HashSet<>(all.size() * 2);
        for (DdiSupportedDrug drug : all) {
            if (drug.getDrugbankId() == null || drug.getDrugbankId().isBlank()) {
                continue;
            }
            ids.add(drug.getDrugbankId().toUpperCase());
            if (drug.getSmilesHash() != null && !drug.getSmilesHash().isBlank()) {
                hashIndex.put(drug.getSmilesHash(), drug.getDrugbankId());
            }
        }
        this.hashToId = hashIndex;
        this.supportedIds = ids;
        log.info("DDI 支持药物清单已加载: {} 条（可按 SMILES 反查 {} 条）", ids.size(), hashIndex.size());
    }

    /** 与清单生成脚本保持一致的规范化：仅去除所有空白字符。 */
    static String normalize(String smiles) {
        return smiles.replaceAll("\\s+", "");
    }

    static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM 不支持 SHA-256", e);
        }
    }
}
