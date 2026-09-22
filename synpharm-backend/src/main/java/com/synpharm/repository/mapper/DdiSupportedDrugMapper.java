package com.synpharm.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synpharm.model.entity.DdiSupportedDrug;
import org.apache.ibatis.annotations.Mapper;

/**
 * DDI 支持药物白名单数据访问接口。
 *
 * <p>白名单共约 1300 条，由 {@link com.synpharm.pipeline.resolve.DdiDrugResolver}
 * 一次性全量加载进内存做 SMILES 反查，不做逐条查询。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Mapper
public interface DdiSupportedDrugMapper extends BaseMapper<DdiSupportedDrug> {
}
