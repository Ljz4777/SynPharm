-- =============================================
-- 安全一致性迁移（幂等，重复执行安全）
-- 版本：v5.0.0
-- 内容：
--   1. sys_user.token_version        —— JWT 版本号（B-04 验人：改密/重置后 +1，旧 token 立即失效）
--   2. predict_result.fingerprint    —— 预测指纹列 + 唯一索引（C-07 幂等，MySQL 唯一索引允许多个 NULL）
--   3. sys_login_log 外键            —— user_id -> sys_user(id)（F-05，先清孤儿行再建约束）
--   4. predict_task/predict_result.update_time —— 更新时间列（F-05，DB 自动维护）
-- =============================================

USE synpharm;

-- ---------- 1. sys_user.token_version ----------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'token_version'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN token_version INT NOT NULL DEFAULT 0 COMMENT ''JWT 版本号：改密/重置后+1，旧 token 立即失效'' AFTER status',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. predict_result.fingerprint ----------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'predict_result' AND COLUMN_NAME = 'fingerprint'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE predict_result ADD COLUMN fingerprint VARCHAR(64) DEFAULT NULL COMMENT ''预测指纹 sha256(userId|algoType|归一化输入)'' AFTER result_no',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'predict_result' AND INDEX_NAME = 'uk_fingerprint'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE predict_result ADD UNIQUE KEY uk_fingerprint (fingerprint)',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 3. sys_login_log 外键（先清孤儿行） ----------
DELETE FROM sys_login_log
WHERE user_id IS NOT NULL AND user_id NOT IN (SELECT id FROM sys_user);

SET @fk_exists := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'sys_login_log' AND CONSTRAINT_NAME = 'fk_login_log_user'
);
SET @ddl := IF(@fk_exists = 0,
    'ALTER TABLE sys_login_log ADD CONSTRAINT fk_login_log_user FOREIGN KEY (user_id) REFERENCES sys_user(id)',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 4. update_time 列 ----------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'predict_task' AND COLUMN_NAME = 'update_time'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE predict_task ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER created_at',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'predict_result' AND COLUMN_NAME = 'update_time'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE predict_result ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER created_at',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
