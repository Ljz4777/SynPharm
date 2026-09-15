package com.synpharm.dto.response;

import com.synpharm.model.entity.SysLoginLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录记录响应DTO
 *
 * <p>用于个人中心的「登录记录」展示。数据来自 sys_login_log，
 * 查询时已按当前登录用户过滤，故不做字段脱敏。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@Builder
public class LoginLogResponse {

    /** 登录账号 */
    private String account;

    /** 登录类型：qq_email / phone / wechat / password */
    private String loginType;

    /** 登录IP */
    private String loginIp;

    /** 登录地点（IP 解析，预留字段） */
    private String loginLocation;

    /** 浏览器/设备信息 */
    private String userAgent;

    /** 是否登录成功 */
    private Boolean success;

    /** 失败原因 */
    private String failReason;

    /** 登录时间 */
    private LocalDateTime createdAt;

    public static LoginLogResponse fromEntity(SysLoginLog entity) {
        return LoginLogResponse.builder()
                .account(entity.getAccount())
                .loginType(entity.getLoginType())
                .loginIp(entity.getLoginIp())
                .loginLocation(entity.getLoginLocation())
                .userAgent(truncate(entity.getUserAgent(), 160))
                .success(Integer.valueOf(1).equals(entity.getStatus()))
                .failReason(entity.getFailReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * 截断过长的 UA：部分浏览器 UA 可超 300 字符，
     * 前端列表无需完整展示，保留前 160 字符即可。
     */
    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max) + "…";
    }
}
