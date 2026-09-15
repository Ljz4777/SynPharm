-- =============================================
-- 用户科研档案字段（sys_user）
-- 版本：v3.1.0
-- =============================================
--
-- 背景：个人中心需要展示/维护科研身份信息。ORCID iD 是生物信息领域通行的
--       研究者唯一标识（形如 0000-0002-1825-0097），用于协作与结果归属。
--
-- 用法：
--   01_sys_user.sql 只会在 MySQL 数据卷为空时自动执行；已部署的环境不会重跑，
--   因此本文件需要手工执行一次：
--     docker exec -i synpharm-mysql mysql -uroot -p<pwd> synpharm < 09_sys_user_research_profile.sql
--   注意：ALTER TABLE ... ADD COLUMN 不幂等，重复执行会报 Duplicate column。
-- =============================================

USE synpharm;

ALTER TABLE sys_user
    ADD COLUMN institution   VARCHAR(128) DEFAULT NULL COMMENT '机构/单位' AFTER register_type,
    ADD COLUMN lab           VARCHAR(128) DEFAULT NULL COMMENT '实验室/课题组' AFTER institution,
    ADD COLUMN orcid         VARCHAR(19)  DEFAULT NULL COMMENT 'ORCID iD，格式 0000-0000-0000-0000' AFTER lab,
    ADD COLUMN research_area VARCHAR(128) DEFAULT NULL COMMENT '研究方向' AFTER orcid;
