-- =============================================
-- SynPharm 批次任务表 (batch_task)
-- 版本：v3.0.0
-- =============================================

USE synpharm;

DROP TABLE IF EXISTS batch_task;
CREATE TABLE batch_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    batch_id VARCHAR(64) NOT NULL COMMENT '唯一批次ID (UUID)',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    file_path VARCHAR(255) NOT NULL COMMENT '原始CSV文件存储路径',
    total_count INT NOT NULL DEFAULT 0 COMMENT '总数据条数',
    success_count INT NOT NULL DEFAULT 0 COMMENT '成功处理条数',
    fail_count INT NOT NULL DEFAULT 0 COMMENT '失败条数',
    progress DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '当前进度 (0.00-100.00)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0:PENDING, 1:PROCESSING, 2:SUCCESS, 3:FAIL',
    result_url VARCHAR(255) DEFAULT NULL COMMENT '结果文件下载地址',
    error_msg TEXT DEFAULT NULL COMMENT '批次级错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_batch_id (batch_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批次任务表';