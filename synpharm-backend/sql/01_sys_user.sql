-- =============================================
-- SynPharm 用户表 (sys_user)
-- 支持多种登录方式（QQ邮箱、手机号、微信等）关联到同一个用户
-- 版本：v3.0.0
-- =============================================

USE synpharm;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    email VARCHAR(100) DEFAULT NULL COMMENT '用户邮箱（QQ邮箱登录用）',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号（手机号登录用，预留）',
    password VARCHAR(255) DEFAULT NULL COMMENT '密码（BCrypt加密，验证码登录可为空）',
    nickname VARCHAR(50) NOT NULL COMMENT '用户昵称',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：admin-管理员 user-普通用户 guest-游客',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    email_verified TINYINT NOT NULL DEFAULT 0 COMMENT '邮箱验证：0未验证 1已验证',
    last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    login_count INT NOT NULL DEFAULT 0 COMMENT '登录次数统计',
    register_type VARCHAR(20) NOT NULL DEFAULT 'qq_email' COMMENT '注册方式：qq_email/phone/wechat',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',

    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_register_type (register_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';