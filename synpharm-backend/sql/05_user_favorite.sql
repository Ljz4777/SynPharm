-- =============================================
-- SynPharm 用户收藏表 (user_favorite)
-- 版本：v3.0.0
-- =============================================

USE synpharm;

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