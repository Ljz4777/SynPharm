-- =============================================
-- SynPharm 测试数据
-- 版本：v3.0.0
-- =============================================

USE synpharm;

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