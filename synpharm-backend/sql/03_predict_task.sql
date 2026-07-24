-- =============================================
-- SynPharm 预测任务表 (predict_task)
-- 版本：v3.0.0
-- =============================================

USE synpharm;

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