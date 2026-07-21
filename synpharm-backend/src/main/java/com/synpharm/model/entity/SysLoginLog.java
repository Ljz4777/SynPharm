package com.synpharm.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * <p>映射数据库表 sys_login_log，记录用户每次登录的详细信息，
 * 用于安全审计和异常检测。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    /** 日志ID（主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（登录成功后填充） */
    private Long userId;

    /** 登录账号（邮箱/手机号/第三方ID） */
    private String account;

    /** 登录类型：qq_email/phone/wechat/password */
    private String loginType;

    /** 登录IP地址 */
    private String loginIp;

    /** 登录地点（IP解析，预留） */
    private String loginLocation;

    /** 浏览器/设备信息 */
    private String userAgent;

    /** 验证码类型：email/sms/image */
    private String captchaType;

    /** 状态：0失败，1成功 */
    private Integer status;

    /** 失败原因 */
    private String failReason;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
