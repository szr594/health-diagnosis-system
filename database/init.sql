-- =============================================================================
-- 大健康行业智能问诊系统 - MySQL 8.0 初始化脚本
-- 说明：创建数据库、数据表及种子数据
-- 执行方式：mysql -u root -p < database/init.sql
-- =============================================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `health_diagnosis_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `health_diagnosis_db`;

-- =============================================================================
-- 2. 用户/患者表
-- =============================================================================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`        VARCHAR(50)  NOT NULL                COMMENT '登录用户名',
  `password`        VARCHAR(100) NOT NULL                COMMENT '登录密码(BCrypt加密)',
  `nickname`        VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
  `real_name`       VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
  `phone`           VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `gender`          TINYINT      DEFAULT 0               COMMENT '性别 0未知 1男 2女',
  `age`             INT          DEFAULT NULL            COMMENT '年龄',
  `height`          DECIMAL(5,1) DEFAULT NULL            COMMENT '身高(cm)',
  `weight`          DECIMAL(5,1) DEFAULT NULL            COMMENT '体重(kg)',
  `allergy_history` VARCHAR(500) DEFAULT NULL            COMMENT '过敏史',
  `medical_history` VARCHAR(500) DEFAULT NULL            COMMENT '既往病史',
  `role`            TINYINT      DEFAULT 0               COMMENT '角色 0患者 1医生 2管理员',
  `avatar`          VARCHAR(255) DEFAULT NULL            COMMENT '头像URL',
  `status`          TINYINT      DEFAULT 1               COMMENT '状态 0禁用 1启用',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '用户/患者表';

-- =============================================================================
-- 3. 问诊记录表
-- =============================================================================
DROP TABLE IF EXISTS `consultation_record`;
CREATE TABLE `consultation_record` (
  `id`                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`             BIGINT        NOT NULL                COMMENT '患者用户ID',
  `session_id`          BIGINT        DEFAULT NULL            COMMENT '关联会话ID',
  `symptom_description` VARCHAR(2000) NOT NULL                COMMENT '症状描述',
  `symptom_duration`    VARCHAR(50)   DEFAULT NULL            COMMENT '症状持续时间',
  `chief_complaint`     VARCHAR(500)  DEFAULT NULL            COMMENT '主诉(精简)',
  `ai_advice`           TEXT          DEFAULT NULL            COMMENT 'AI问诊建议(完整文本)',
  `structured_advice`   TEXT          DEFAULT NULL            COMMENT '结构化建议(JSON)',
  `risk_level`          VARCHAR(20)   DEFAULT NULL            COMMENT '风险等级 low低风险 / mid中风险 / high高风险',
  `possible_diseases`   VARCHAR(500)  DEFAULT NULL            COMMENT '疑似疾病(逗号分隔)',
  `suggested_department` VARCHAR(50)  DEFAULT NULL            COMMENT '建议就诊科室',
  `status`              TINYINT       DEFAULT 0               COMMENT '状态 0处理中 1完成 2失败',
  `fail_reason`         VARCHAR(500)  DEFAULT NULL            COMMENT '失败原因(降级说明)',
  `create_time`         DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`         DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE = InnoDB COMMENT = 'AI问诊记录表';

-- =============================================================================
-- 4. 健康知识文档表（MySQL 中保存原文，向量化后存 ChromaDB）
-- =============================================================================
DROP TABLE IF EXISTS `health_knowledge`;
CREATE TABLE `health_knowledge` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title`       VARCHAR(200) NOT NULL                COMMENT '知识文档标题',
  `category`    VARCHAR(50)  DEFAULT NULL            COMMENT '分类(内科/心血管/呼吸/消化等)',
  `content`     TEXT         NOT NULL                COMMENT '知识内容(原文)',
  `source`      VARCHAR(200) DEFAULT NULL            COMMENT '来源',
  `status`      TINYINT      DEFAULT 1               COMMENT '状态 0下架 1上架',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '健康知识文档表';

-- =============================================================================
-- 5. 问诊会话表（多轮对话状态管理）
-- =============================================================================
DROP TABLE IF EXISTS `diagnosis_session`;
CREATE TABLE `diagnosis_session` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_key`   VARCHAR(36)  NOT NULL                COMMENT '会话唯一标识(UUID)',
  `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
  `status`        TINYINT      DEFAULT 0               COMMENT '状态 0进行中 1已完成 2已归档',
  `summary`       TEXT         DEFAULT NULL            COMMENT 'AI生成的会话摘要(压缩态)',
  `message_count` INT          DEFAULT 0               COMMENT '消息总数',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_key` (`session_key`),
  KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB COMMENT = '问诊会话表：一次完整的多轮问诊会话';

-- =============================================================================
-- 6. 问诊对话历史表
-- =============================================================================
DROP TABLE IF EXISTS `chat_history`;
CREATE TABLE `chat_history` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
  `consultation_id` BIGINT       DEFAULT NULL            COMMENT '关联问诊记录ID',
  `session_id`      BIGINT       DEFAULT NULL            COMMENT '关联会话ID（多轮对话）',
  `role`            VARCHAR(20)  NOT NULL                COMMENT '角色 user用户 / assistant AI',
  `content`         TEXT         NOT NULL                COMMENT '对话内容',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_consult` (`user_id`, `consultation_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE = InnoDB COMMENT = '问诊对话历史表';

-- =============================================================================
-- 6. 种子数据
-- =============================================================================

-- 6.1 用户（密码均为 123456，BCrypt 加密）
--     admin / 123456（管理员），patient1 / 123456（患者）
INSERT INTO `t_user`
  (`username`, `password`, `nickname`, `real_name`, `phone`, `gender`, `age`,
   `height`, `weight`, `allergy_history`, `medical_history`, `role`, `status`)
VALUES
  ('admin', '$2a$10$jIXhuHI39rPZIoVl.eUs8OMR1myvr2x5dPtDiFLluCxKeViaUT7vi',
   '系统管理员', '张三', '13800000000', 1, 30, 175.0, 68.0, '无', '无', 2, 1),
  ('patient1', '$2a$10$jIXhuHI39rPZIoVl.eUs8OMR1myvr2x5dPtDiFLluCxKeViaUT7vi',
   '健康小张', '张女士', '13900000000', 2, 28, 162.0, 52.0, '青霉素', '轻度贫血', 0, 1);

-- 6.2 示例健康知识文档（向量化后用于 RAG 检索）
INSERT INTO `health_knowledge` (`title`, `category`, `content`, `source`, `status`) VALUES
('高血压的常见症状与日常管理', '心血管',
 '高血压早期常无明显症状，部分患者会出现头痛、头晕、颈项板紧、疲劳、心悸等表现。长期高血压可损伤心、脑、肾等重要器官。日常管理建议：1) 定期测量血压并记录；2) 低盐饮食，每日食盐摄入不超过5克；3) 适量运动，每周至少150分钟中等强度有氧运动；4) 戒烟限酒，控制体重；5) 遵医嘱规律服药，不可自行停药或减量。收缩压≥140mmHg或舒张压≥90mmHg时应及时就医。',
 '医学常识科普', 1),
('发热的分级处理与就医建议', '呼吸',
 '体温≥37.3℃视为发热。低热(37.3-38℃)可先物理降温、多饮水、休息观察；中度发热(38.1-39℃)可酌情使用退热药，注意补充水分；高热(≥39.1℃)或持续发热超过3天、伴随呼吸困难、意识障碍、皮疹等症状时，应立即就医。儿童高热易引发惊厥，3月龄以下婴儿发热应尽早就诊。',
 '医学常识科普', 1),
('胸闷胸痛的紧急识别与处理', '心血管',
 '胸闷胸痛是需高度警惕的症状。若为压榨样、紧缩感疼痛，向肩背、下颌放射，持续超过5分钟，或伴大汗、恶心、濒死感，高度怀疑急性心肌梗死，应立即拨打急救电话，保持静卧，避免活动。有冠心病史者应按医嘱舌下含服硝酸甘油。胸痛缓解后也应尽快就诊排查原因，切勿因症状缓解而忽视。',
 '医学常识科普', 1),
('糖尿病的典型症状与血糖管理', '内分泌',
 '糖尿病典型症状为三多一少：多饮、多食、多尿、体重下降。部分患者无明显症状，通过体检发现血糖升高。血糖管理建议：1) 控制主食摄入量，粗细搭配；2) 规律监测空腹及餐后血糖；3) 坚持适度运动；4) 遵医嘱用药或胰岛素治疗；5) 定期检查糖化血红蛋白、眼底及肾功能。空腹血糖≥7.0mmol/L或餐后2小时血糖≥11.1mmol/L需内分泌科就诊。',
 '医学常识科普', 1),
('胃肠道不适的常见原因与生活方式干预', '消化',
 '腹痛、腹胀、反酸、腹泻等胃肠道不适常见原因包括饮食不节、幽门螺杆菌感染、功能性消化不良、胃炎、肠易激综合征等。生活方式建议：1) 规律三餐，避免暴饮暴食与辛辣刺激食物；2) 少饮浓茶咖啡与酒精；3) 出现黑便、呕血、持续消瘦、吞咽困难等警示症状须立即就医；4) 反复不适超过2周建议消化内科就诊，必要时行胃镜、肠镜检查。',
 '医学常识科普', 1),
('失眠的表现与改善建议', '精神心理',
 '失眠指入睡困难、睡眠维持困难或早醒，每周发生3次以上并影响日间功能。改善建议：1) 固定作息时间，规律起床；2) 睡前一小时避免使用手机等电子设备；3) 避免咖啡因、尼古丁与酒精的晚间摄入；4) 创造安静、黑暗、凉爽的睡眠环境；5) 白天适度运动但避免睡前剧烈运动；6) 若持续超过一个月或伴明显焦虑抑郁情绪，建议精神心理科或睡眠门诊就诊。',
 '医学常识科普', 1);

-- 6.3 示例问诊记录（演示数据）
INSERT INTO `consultation_record`
  (`user_id`, `symptom_description`, `symptom_duration`, `chief_complaint`,
   `ai_advice`, `risk_level`, `possible_diseases`, `suggested_department`, `status`)
VALUES
  (2, '最近一周头晕头痛，测量血压偏高约150/95，睡眠不太好', '约一周',
   '头晕头痛伴血压偏高',
   '根据您的症状描述，近期血压明显偏高（150/95mmHg），结合头晕、头痛表现，提示可能存在高血压。建议尽快至心血管内科就诊，完善诊室血压复测及必要的检查。就诊前请每日早、晚各测量一次血压并记录。',
   'mid', '高血压', '心血管内科', 1);

-- =============================================================================
-- 7. 科室表
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
-- 8. 医生信息表（关联 t_user + t_department）
-- =============================================================================
DROP TABLE IF EXISTS `t_doctor`;
CREATE TABLE `t_doctor` (
  `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`          BIGINT         NOT NULL                COMMENT '关联用户ID(t_user.id)',
  `department_id`    BIGINT         DEFAULT NULL            COMMENT '所属科室ID',
  `title`            VARCHAR(50)    DEFAULT NULL            COMMENT '职称',
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
-- 9. 预约挂号表
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
-- 10. 电子病历表
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
-- 11. 业务模块种子数据
-- =============================================================================

-- 11.1 科室
INSERT INTO `t_department` (`name`, `code`, `description`, `location`, `sort_order`, `status`) VALUES
('心血管内科', 'CARDIO', '诊治高血压、冠心病、心律失常、心力衰竭等心血管系统疾病', '门诊楼3楼A区', 1, 1),
('呼吸内科', 'RESPI', '诊治肺炎、哮喘、慢性阻塞性肺疾病、肺结节等呼吸系统疾病', '门诊楼3楼B区', 2, 1),
('消化内科', 'GASTRO', '诊治胃炎、胃溃疡、肝炎、炎症性肠病等消化系统疾病', '门诊楼4楼A区', 3, 1),
('内分泌科', 'ENDOCRINE', '诊治糖尿病、甲状腺疾病、肥胖症、骨质疏松等内分泌代谢疾病', '门诊楼4楼B区', 4, 1),
('神经内科', 'NEURO', '诊治头痛、头晕、脑血管病、癫痫、帕金森等神经系统疾病', '门诊楼5楼A区', 5, 1),
('精神心理科', 'PSYCH', '诊治失眠、焦虑、抑郁、强迫症等精神心理疾病', '门诊楼5楼B区', 6, 1);

-- 11.2 医生用户（密码 123456）
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

-- 11.3 医生信息（关联 user_id = 3,4,5）
INSERT INTO `t_doctor`
  (`user_id`, `department_id`, `title`, `specialty`, `description`, `consultation_fee`, `max_daily_appointments`, `status`)
VALUES
  (3, 1, '主任医师', '高血压、冠心病、心律失常', '从事心血管内科临床工作20年，擅长复杂心血管疾病的诊断与治疗。', 50.00, 15, 1),
  (4, 4, '副主任医师', '糖尿病、甲状腺疾病、肥胖症', '内分泌科资深专家，对糖尿病综合管理有丰富经验。', 35.00, 20, 1),
  (5, 2, '主治医师', '哮喘、慢阻肺、肺部感染', '呼吸内科骨干医师，擅长呼吸系统感染性疾病的诊治。', 25.00, 25, 1);

-- 11.4 示例预约
INSERT INTO `t_appointment`
  (`appointment_no`, `patient_id`, `doctor_id`, `department_id`, `appointment_date`, `time_slot`, `reason`, `status`)
VALUES
  ('AP20260811001', 2, 1, 1, '2026-08-12', '上午', '头晕头痛，血压偏高', 1),
  ('AP20260811002', 2, 2, 4, '2026-08-13', '下午', '口渴多饮，疑似血糖偏高', 0);
