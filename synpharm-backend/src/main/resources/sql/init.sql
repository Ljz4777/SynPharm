CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE DEFAULT NULL,
    phone VARCHAR(20) UNIQUE DEFAULT NULL,
    password VARCHAR(255) DEFAULT NULL,
    nickname VARCHAR(50) DEFAULT 'Guest',
    avatar_url VARCHAR(255) DEFAULT NULL,
    role VARCHAR(20) DEFAULT 'user',
    status INT DEFAULT 1,
    email_verified INT DEFAULT 0,
    last_login_at DATETIME DEFAULT NULL,
    last_login_ip VARCHAR(50) DEFAULT NULL,
    login_count INT DEFAULT 0,
    register_type VARCHAR(20) DEFAULT 'guest',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    account VARCHAR(100) DEFAULT NULL,
    login_type VARCHAR(20) DEFAULT NULL,
    login_ip VARCHAR(50) DEFAULT NULL,
    login_location VARCHAR(100) DEFAULT NULL,
    user_agent VARCHAR(500) DEFAULT NULL,
    captcha_type VARCHAR(20) DEFAULT NULL,
    status INT DEFAULT 0,
    fail_reason VARCHAR(200) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_login_ip (login_ip),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS predict_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT DEFAULT NULL,
    predict_type VARCHAR(20) DEFAULT NULL,
    input_type VARCHAR(20) DEFAULT NULL,
    input_value TEXT DEFAULT NULL,
    file_url VARCHAR(255) DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    progress INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_task_no (task_no),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测任务表';

CREATE TABLE IF NOT EXISTS predict_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT DEFAULT NULL,
    target_id VARCHAR(100) DEFAULT NULL,
    target_name VARCHAR(200) DEFAULT NULL,
    binding_affinity DOUBLE DEFAULT NULL,
    confidence_score DOUBLE DEFAULT NULL,
    confidence_level VARCHAR(20) DEFAULT NULL,
    interactions TEXT DEFAULT NULL,
    result_data TEXT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_task_id (task_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测结果表';

CREATE TABLE IF NOT EXISTS batch_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT DEFAULT NULL,
    file_path VARCHAR(255) DEFAULT NULL,
    total_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    fail_count INT DEFAULT 0,
    progress DECIMAL(5,2) DEFAULT 0,
    status INT DEFAULT 0,
    result_url VARCHAR(255) DEFAULT NULL,
    error_msg TEXT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_batch_id (batch_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批量任务表';

INSERT IGNORE INTO sys_user (email, nickname, role, status, email_verified, register_type) VALUES
('admin@synpharm.com', 'Admin', 'admin', 1, 1, 'qq_email'),
('guest@synpharm.com', 'Guest', 'user', 1, 0, 'guest');