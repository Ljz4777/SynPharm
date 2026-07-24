-- =============================================
-- SynPharm 登录日志表 (sys_login_log)
-- 记录用户每次登录的详细信息，用于安全审计和异常检测
-- 版本：v3.0.0
-- =============================================

USE synpharm;

DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID（登录成功后填充）',
    account VARCHAR(100) NOT NULL COMMENT '登录账号（邮箱/手机号/第三方ID）',
    login_type VARCHAR(20) NOT NULL COMMENT '登录类型：qq_email/phone/wechat/password',
    login_ip VARCHAR(50) DEFAULT NULL COMMENT '登录IP地址',
    login_location VARCHAR(100) DEFAULT NULL COMMENT '登录地点（IP解析，预留）',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '浏览器/设备信息',
    captcha_type VARCHAR(20) DEFAULT NULL COMMENT '验证码类型：email/sms/image',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0失败 1成功',
    fail_reason VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_login_type (login_type),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';