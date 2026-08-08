-- =============================================
-- SynPharm batch_task 表结构迁移
-- 为批量任务消息队列（RabbitMQ）改造补充列
-- 版本：v3.1.0
-- 说明：幂等执行，重复运行报错可忽略（列已存在）
-- =============================================

USE synpharm;

-- 算法类型（持久化，避免依赖 Redis 缓存）
ALTER TABLE batch_task
    ADD COLUMN algo_type VARCHAR(20) NOT NULL DEFAULT '' COMMENT '算法类型 DTI/PPI/DDI' AFTER status;

-- 逻辑删除（与 Mapper XML 的 deleted=0 查询一致）
ALTER TABLE batch_task
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除' AFTER update_time;
