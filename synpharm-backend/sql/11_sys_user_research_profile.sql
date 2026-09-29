-- =============================================
-- 用户科研档案字段（sys_user）
-- 版本：v3.1.0（v5.0.0 修复：改为幂等，重复执行安全）
-- =============================================
--
-- 背景：个人中心需要展示/维护科研身份信息。ORCID iD 是生物信息领域通行的
--       研究者唯一标识（形如 0000-0002-1825-0097），用于协作与结果归属。
--
-- 用法：
--   新库初始化时由 docker-entrypoint-initdb.d 自动执行；
--   已部署的库手工执行一次即可，重复执行不再报 Duplicate column。
-- =============================================

USE synpharm;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'institution'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN institution VARCHAR(128) DEFAULT NULL COMMENT ''机构/单位'' AFTER register_type',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'lab'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN lab VARCHAR(128) DEFAULT NULL COMMENT ''实验室/课题组'' AFTER institution',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'orcid'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN orcid VARCHAR(19) DEFAULT NULL COMMENT ''ORCID iD，格式 0000-0000-0000-0000'' AFTER lab',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'synpharm' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'research_area'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN research_area VARCHAR(128) DEFAULT NULL COMMENT ''研究方向'' AFTER orcid',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
