-- =============================================================================
-- 业务模块迁移脚本 v3：科室 + 医生 + 预约挂号 + 电子病历
-- 执行方式：mysql -u root -p health_diagnosis_db < database/migration_v3.sql
-- =============================================================================

USE `health_diagnosis_db`;

-- =============================================================================
-- 1. 科室表
-- =============================================================================
DROP TABLE IF EXISTS `t_department`;
CREATE TABLE `t_department` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(100) NOT NULL                COMMENT '科室名称',
  `code`        VARCHAR(50)  DEFAULT NULL            COMMENT '科室编码',
  `description` VARCHAR(500) DEFAULT NULL            COMMENT '科室简介',
  `location`    VARCHAR(200) DEFAULT NULL            COMMENT '科室位置(楼层/诊区)',
  `sort_order`  INT          DEFAULT 0               COMMENT '排序序号',
  `status`      TINYINT      DEFAULT 1               COMMENT '状态 0停用 1启用',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB COMMENT = '科室表';

-- =============================================================================
-- 2. 医生表（关联 t_user.user_id + t_department.id）
-- =============================================================================
DROP TABLE IF EXISTS `t_doctor`;
CREATE TABLE `t_doctor` (
  `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`          BIGINT         NOT NULL                COMMENT '关联用户ID(t_user.id)',
  `department_id`    BIGINT         DEFAULT NULL            COMMENT '所属科室ID',
  `title`            VARCHAR(50)    DEFAULT NULL            COMMENT '职称(主任医师/副主任医师/主治医师/住院医师)',
  `specialty`        VARCHAR(500)   DEFAULT NULL            COMMENT '擅长领域',
  `description`      TEXT           DEFAULT NULL            COMMENT '医生简介',
  `consultation_fee` DECIMAL(10,2)  DEFAULT 0.00            COMMENT '挂号费(元)',
  `max_daily_appointments` INT      DEFAULT 20              COMMENT '每日最大接诊量',
  `status`           TINYINT        DEFAULT 1               COMMENT '状态 0停诊 1接诊',
  `create_time`      DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_department_id` (`department_id`)
) ENGINE = InnoDB COMMENT = '医生信息表';

-- =============================================================================
-- 3. 预约挂号表
-- =============================================================================
DROP TABLE IF EXISTS `t_appointment`;
CREATE TABLE `t_appointment` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `appointment_no`    VARCHAR(32)  NOT NULL                COMMENT '预约编号(唯一)',
  `patient_id`        BIGINT       NOT NULL                COMMENT '患者用户ID',
  `doctor_id`         BIGINT       NOT NULL                COMMENT '医生ID',
  `department_id`     BIGINT       NOT NULL                COMMENT '科室ID',
  `appointment_date`  DATE         NOT NULL                COMMENT '预约就诊日期',
  `time_slot`         VARCHAR(20)  NOT NULL                COMMENT '时段 上午/下午/晚间',
  `reason`            VARCHAR(500) DEFAULT NULL            COMMENT '就诊原因/症状简述',
  `status`            TINYINT      DEFAULT 0               COMMENT '状态 0待确认 1已确认 2已完成 3已取消',
  `cancel_reason`     VARCHAR(500) DEFAULT NULL            COMMENT '取消原因',
  `remark`            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appointment_no` (`appointment_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_doctor_date` (`doctor_id`, `appointment_date`)
) ENGINE = InnoDB COMMENT = '预约挂号表';

-- =============================================================================
-- 4. 电子病历表
-- =============================================================================
DROP TABLE IF EXISTS `t_medical_record`;
CREATE TABLE `t_medical_record` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_no`        VARCHAR(32)   NOT NULL                COMMENT '病历编号(唯一)',
  `patient_id`       BIGINT        NOT NULL                COMMENT '患者用户ID',
  `doctor_id`        BIGINT        NOT NULL                COMMENT '医生ID',
  `department_id`    BIGINT        DEFAULT NULL            COMMENT '科室ID',
  `appointment_id`   BIGINT        DEFAULT NULL            COMMENT '关联预约ID',
  `chief_complaint`  VARCHAR(1000) DEFAULT NULL            COMMENT '主诉',
  `present_illness`  TEXT          DEFAULT NULL            COMMENT '现病史',
  `past_history`     TEXT          DEFAULT NULL            COMMENT '既往史',
  `physical_exam`    TEXT          DEFAULT NULL            COMMENT '体格检查',
  `diagnosis`        VARCHAR(1000) DEFAULT NULL            COMMENT '诊断结果',
  `treatment_plan`   TEXT          DEFAULT NULL            COMMENT '治疗方案',
  `prescription`     TEXT          DEFAULT NULL            COMMENT '处方信息',
  `remark`           VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  `create_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_appointment_id` (`appointment_id`)
) ENGINE = InnoDB COMMENT = '电子病历表';

-- =============================================================================
-- 5. 种子数据
-- =============================================================================

-- 5.1 科室
INSERT INTO `t_department` (`name`, `code`, `description`, `location`, `sort_order`, `status`) VALUES
('心血管内科', 'CARDIO', '诊治高血压、冠心病、心律失常、心力衰竭等心血管系统疾病', '门诊楼3楼A区', 1, 1),
('呼吸内科', 'RESPI', '诊治肺炎、哮喘、慢性阻塞性肺疾病、肺结节等呼吸系统疾病', '门诊楼3楼B区', 2, 1),
('消化内科', 'GASTRO', '诊治胃炎、胃溃疡、肝炎、炎症性肠病等消化系统疾病', '门诊楼4楼A区', 3, 1),
('内分泌科', 'ENDOCRINE', '诊治糖尿病、甲状腺疾病、肥胖症、骨质疏松等内分泌代谢疾病', '门诊楼4楼B区', 4, 1),
('神经内科', 'NEURO', '诊治头痛、头晕、脑血管病、癫痫、帕金森等神经系统疾病', '门诊楼5楼A区', 5, 1),
('精神心理科', 'PSYCH', '诊治失眠、焦虑、抑郁、强迫症等精神心理疾病', '门诊楼5楼B区', 6, 1);

-- 5.2 医生用户（密码 123456）—— 先在 t_user 注册医生角色用户
INSERT INTO `t_user`
  (`username`, `password`, `nickname`, `real_name`, `phone`, `gender`, `age`,
   `height`, `weight`, `allergy_history`, `medical_history`, `role`, `status`)
VALUES
  ('doctor1', '$2a$10$jIXhuHI39rPZIoVl.eUs8OMR1myvr2x5dPtDiFLluCxKeViaUT7vi',
   '李医生', '李明', '13700000001', 1, 45, 178.0, 75.0, '无', '无', 1, 1),
  ('doctor2', '$2a$10$jIXhuHI39rPZIoVl.eUs8OMR1myvr2x5dPtDiFLluCxKeViaUT7vi',
   '王医生', '王芳', '13700000002', 2, 38, 165.0, 58.0, '无', '无', 1, 1),
  ('doctor3', '$2a$10$jIXhuHI39rPZIoVl.eUs8OMR1myvr2x5dPtDiFLluCxKeViaUT7vi',
   '赵医生', '赵强', '13700000003', 1, 50, 172.0, 70.0, '无', '无', 1, 1);

-- 5.3 医生信息（关联 user_id = 3,4,5 → doctor1, doctor2, doctor3）
INSERT INTO `t_doctor`
  (`user_id`, `department_id`, `title`, `specialty`, `description`, `consultation_fee`, `max_daily_appointments`, `status`)
VALUES
  (3, 1, '主任医师', '高血压、冠心病、心律失常', '从事心血管内科临床工作20年，擅长复杂心血管疾病的诊断与治疗。', 50.00, 15, 1),
  (4, 4, '副主任医师', '糖尿病、甲状腺疾病、肥胖症', '内分泌科资深专家，对糖尿病综合管理有丰富经验。', 35.00, 20, 1),
  (5, 2, '主治医师', '哮喘、慢阻肺、肺部感染', '呼吸内科骨干医师，擅长呼吸系统感染性疾病的诊治。', 25.00, 25, 1);

-- 5.4 示例预约
INSERT INTO `t_appointment`
  (`appointment_no`, `patient_id`, `doctor_id`, `department_id`, `appointment_date`, `time_slot`, `reason`, `status`)
VALUES
  ('AP20260811001', 2, 1, 1, '2026-08-12', '上午', '头晕头痛，血压偏高', 1),
  ('AP20260811002', 2, 2, 4, '2026-08-13', '下午', '口渴多饮，疑似血糖偏高', 0);
