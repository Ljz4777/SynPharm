-- =============================================
-- SynPharm 数据库建表脚本
-- 数据库名称：synpharm
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci
-- 版本：v3.0.0
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS synpharm
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE synpharm;

-- =============================================
-- 1. 用户表 (sys_user)
-- 支持多种登录方式（QQ邮箱、手机号、微信等）关联到同一个用户
-- =============================================
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

-- =============================================
-- 2. 登录日志表 (sys_login_log)
-- 记录用户每次登录的详细信息，用于安全审计和异常检测
-- =============================================
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

-- =============================================
-- 3. 预测任务表 (predict_task)
-- =============================================
DROP TABLE IF EXISTS predict_task;
CREATE TABLE predict_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_no VARCHAR(32) NOT NULL UNIQUE COMMENT '任务编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    predict_type VARCHAR(20) NOT NULL COMMENT '预测类型：ppi-蛋白质相互作用 dti-药物靶点相互作用 ddi-药物相互作用',
    input_type VARCHAR(20) NOT NULL COMMENT '输入类型：smiles/pdb/uniprot/csv',
    input_value TEXT NOT NULL COMMENT '输入值',
    file_url VARCHAR(500) COMMENT '上传文件URL',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待处理 running-进行中 completed-已完成 failed-失败 cancelled-已取消',
    progress INT NOT NULL DEFAULT 0 COMMENT '进度百分比 0-100',
    error_message TEXT COMMENT '错误信息',
    ai_task_id VARCHAR(100) COMMENT 'AI服务任务ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    started_at DATETIME COMMENT '开始时间',
    completed_at DATETIME COMMENT '完成时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_task_no (task_no),
    INDEX idx_created_at (created_at),
    INDEX idx_predict_type (predict_type),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测任务表';

-- =============================================
-- 4. 预测结果表 (predict_result)
-- =============================================
DROP TABLE IF EXISTS predict_result;
CREATE TABLE predict_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
    result_no VARCHAR(32) NOT NULL UNIQUE COMMENT '结果编号',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id VARCHAR(100) COMMENT '靶点ID',
    target_name VARCHAR(200) COMMENT '靶点名称',
    ligand_smiles TEXT COMMENT '配体SMILES',
    binding_affinity DECIMAL(10, 4) COMMENT '结合亲和力',
    confidence_score DECIMAL(5, 4) COMMENT '置信度 0-1',
    confidence_level VARCHAR(10) COMMENT '置信等级：high/medium/low',
    prediction_data JSON COMMENT '完整预测数据',
    interactions JSON COMMENT '相互作用详情JSON',
    dataset_source VARCHAR(100) COMMENT '数据集来源',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    INDEX idx_task_id (task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_result_no (result_no),
    INDEX idx_confidence (confidence_score),
    INDEX idx_target_id (target_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (task_id) REFERENCES predict_task(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测结果表';

-- =============================================
-- 5. 用户收藏表 (user_favorite)
-- =============================================
DROP TABLE IF EXISTS user_favorite;
CREATE TABLE user_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    result_id BIGINT NOT NULL COMMENT '结果ID',
    note TEXT COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    UNIQUE KEY uk_user_result (user_id, result_id),
    INDEX idx_user_id (user_id),
    INDEX idx_result_id (result_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (result_id) REFERENCES predict_result(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- =============================================
-- 测试数据
-- =============================================

-- 插入测试管理员（密码是 123456 的BCrypt加密）
INSERT INTO sys_user (email, password, nickname, role, status, email_verified, register_type, login_count) VALUES
('admin@synpharm.com', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj3C6', '管理员', 'admin', 1, 1, 'qq_email', 0);

-- 插入测试任务
INSERT INTO predict_task (task_no, user_id, predict_type, input_type, input_value, status, progress, created_at) VALUES
('task_20240101120001', 1, 'dti', 'smiles', 'CC(=O)OC1=CC=CC=C1C(=O)O,P00533', 'completed', 100, '2024-01-01 12:00:00'),
('task_20240101120002', 1, 'ppi', 'uniprot', 'P00533,P07333', 'completed', 100, '2024-01-01 13:00:00'),
('task_20240101120003', 1, 'ddi', 'smiles', 'CC(=O)OC1=CC=CC=C1C(=O)O,c1ccccc1', 'pending', 0, '2024-01-01 14:00:00');

-- 插入测试结果
INSERT INTO predict_result (result_no, task_id, user_id, target_id, target_name, ligand_smiles, binding_affinity, confidence_score, confidence_level, dataset_source, created_at) VALUES
('result_001', 1, 1, 'P00533', 'EGFR', 'CC(=O)OC1=CC=CC=C1C(=O)O', -9.2500, 0.9200, 'high', 'DrugBank', '2024-01-01 12:05:00'),
('result_002', 2, 1, 'P00533', 'EGFR-P07333', NULL, NULL, 0.8500, 'high', 'STRING', '2024-01-01 13:05:00');
