package com.synpharm.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synpharm.model.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志数据访问接口
 *
 * <p>继承MyBatisPlus的BaseMapper，提供登录日志的CRUD操作。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {
}
