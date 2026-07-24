-- =============================================
-- SynPharm 预测结果表 (predict_result)
-- 版本：v3.0.0
-- =============================================

USE synpharm;

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