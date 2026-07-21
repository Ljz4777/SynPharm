package com.synpharm.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synpharm.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 系统用户数据访问接口
 *
 * <p>继承MyBatisPlus的BaseMapper，提供用户数据的CRUD操作。
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据邮箱查询用户
     *
     * @param email 用户邮箱
     * @return 用户实体
     */
    SysUser selectByEmail(@Param("email") String email);

    /**
     * 更新用户登录信息
     * <p>登录成功后调用，更新最后登录时间、IP和登录次数。
     *
     * @param id          用户ID
     * @param lastLoginAt 最后登录时间
     * @param lastLoginIp 最后登录IP
     * @param updatedAt   更新时间
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET last_login_at = #{lastLoginAt}, last_login_ip = #{lastLoginIp}, " +
            "login_count = login_count + 1, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateLoginInfo(@Param("id") Long id,
                        @Param("lastLoginAt") LocalDateTime lastLoginAt,
                        @Param("lastLoginIp") String lastLoginIp,
                        @Param("updatedAt") LocalDateTime updatedAt);
}
