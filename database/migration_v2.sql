-- =============================================================================
-- 多轮对话状态管理 - 数据库迁移脚本
-- 说明：在已有库上执行，新增 diagnosis_session 表 + 为 chat_history 和
--       consultation_record 增加 session_id 列
-- 执行方式：mysql -u root -p health_diagnosis_db < database/migration_v2.sql
-- =============================================================================

USE `health_diagnosis_db`;

-- 1. 新增问诊会话表：记录一次完整的多轮问诊会话
DROP TABLE IF EXISTS `diagnosis_session`;
CREATE TABLE `diagnosis_session` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_key` VARCHAR(36)  NOT NULL                COMMENT '会话唯一标识(UUID)',
  `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
  `status`      TINYINT      DEFAULT 0               COMMENT '状态 0进行中 1已完成 2已归档',
  `summary`     TEXT         DEFAULT NULL            COMMENT 'AI 生成的会话摘要（压缩态，用于 token 预算控制）',
  `message_count` INT        DEFAULT 0               COMMENT '消息总数（user + assistant 合计）',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_key` (`session_key`),
  KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB COMMENT = '问诊会话表：一次完整的多轮问诊会话';

-- 2. chat_history 增加 session_id 列（兼容旧数据，允许 NULL）
ALTER TABLE `chat_history`
  ADD COLUMN `session_id` BIGINT DEFAULT NULL COMMENT '关联会话ID' AFTER `consultation_id`,
  ADD INDEX `idx_session_id` (`session_id`);

-- 3. consultation_record 增加 session_id 列
ALTER TABLE `consultation_record`
  ADD COLUMN `session_id` BIGINT DEFAULT NULL COMMENT '关联会话ID' AFTER `user_id`,
  ADD INDEX `idx_session_id` (`session_id`);
