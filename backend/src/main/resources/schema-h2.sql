-- H2 初始化脚本（兼容 MySQL 模式）
CREATE TABLE IF NOT EXISTS t_user (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  username        VARCHAR(50) NOT NULL UNIQUE,
  password        VARCHAR(100) NOT NULL,
  nickname        VARCHAR(50),
  real_name       VARCHAR(50),
  phone           VARCHAR(20),
  gender          TINYINT DEFAULT 0,
  age             INT,
  height          DECIMAL(5,1),
  weight          DECIMAL(5,1),
  allergy_history VARCHAR(500),
  medical_history VARCHAR(500),
  role            TINYINT DEFAULT 0,
  avatar          VARCHAR(255),
  status          TINYINT DEFAULT 1,
  create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS consultation_record (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id             BIGINT NOT NULL,
  symptom_description VARCHAR(2000) NOT NULL,
  symptom_duration    VARCHAR(50),
  chief_complaint     VARCHAR(500),
  ai_advice           TEXT,
  structured_advice   TEXT,
  risk_level          VARCHAR(20),
  possible_diseases   VARCHAR(500),
  suggested_department VARCHAR(50),
  status              TINYINT DEFAULT 0,
  fail_reason         VARCHAR(500),
  create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS health_knowledge (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(200) NOT NULL,
  category    VARCHAR(50),
  content     TEXT NOT NULL,
  source      VARCHAR(200),
  status      TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_history (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id         BIGINT NOT NULL,
  consultation_id BIGINT,
  role            VARCHAR(20) NOT NULL,
  content         TEXT NOT NULL,
  create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 种子数据
MERGE INTO t_user (username, password, nickname, real_name, phone, gender, age, height, weight, allergy_history, medical_history, role, status) KEY(username) VALUES
  ('admin', '$2a$10$jIXhuHI39rPZIoVl.eUs8OMR1myvr2x5dPtDiFLluCxKeViaUT7vi', '系统管理员', '张三', '13800000000', 1, 30, 175.0, 68.0, '无', '无', 2, 1),
  ('patient1', '$2a$10$jIXhuHI39rPZIoVl.eUs8OMR1myvr2x5dPtDiFLluCxKeViaUT7vi', '健康小张', '张女士', '13900000000', 2, 28, 162.0, 52.0, '青霉素', '轻度贫血', 0, 1);

MERGE INTO health_knowledge (id, title, category, content, source, status) KEY(id) VALUES
(1, '高血压的常见症状与日常管理', '心血管', '高血压早期常无明显症状，部分患者会出现头痛、头晕、颈项板紧、疲劳、心悸等表现。长期高血压可损伤心、脑、肾等重要器官。日常管理建议：1) 定期测量血压并记录；2) 低盐饮食，每日食盐摄入不超过5克；3) 适量运动，每周至少150分钟中等强度有氧运动；4) 戒烟限酒，控制体重；5) 遵医嘱规律服药，不可自行停药或减量。', '医学常识科普', 1),
(2, '发热的分级处理与就医建议', '呼吸', '体温≥37.3℃视为发热。低热(37.3-38℃)可先物理降温、多饮水、休息观察；中度发热(38.1-39℃)可酌情使用退热药，注意补充水分；高热(≥39.1℃)或持续发热超过3天、伴随呼吸困难、意识障碍、皮疹等症状时，应立即就医。', '医学常识科普', 1),
(3, '胸闷胸痛的紧急识别与处理', '心血管', '胸闷胸痛是需高度警惕的症状。若为压榨样、紧缩感疼痛，向肩背、下颌放射，持续超过5分钟，或伴大汗、恶心、濒死感，高度怀疑急性心肌梗死，应立即拨打急救电话。', '医学常识科普', 1),
(4, '糖尿病的典型症状与血糖管理', '内分泌', '糖尿病典型症状为三多一少：多饮、多食、多尿、体重下降。血糖管理建议：控制主食摄入量，规律监测血糖，坚持适度运动，遵医嘱用药。', '医学常识科普', 1),
(5, '胃肠道不适的常见原因与生活方式干预', '消化', '腹痛、腹胀、反酸、腹泻等胃肠道不适常见原因包括饮食不节、幽门螺杆菌感染、功能性消化不良等。建议规律三餐，避免辛辣刺激食物，出现黑便、呕血等警示症状须立即就医。', '医学常识科普', 1),
(6, '失眠的表现与改善建议', '精神心理', '失眠指入睡困难、睡眠维持困难或早醒，每周发生3次以上并影响日间功能。改善建议：固定作息时间，睡前一小时避免使用电子设备，避免咖啡因晚间摄入。', '医学常识科普', 1);

MERGE INTO consultation_record (id, user_id, symptom_description, symptom_duration, chief_complaint, ai_advice, risk_level, possible_diseases, suggested_department, status) KEY(id) VALUES
(1, 2, '最近一周头晕头痛，测量血压偏高约150/95，睡眠不太好', '约一周', '头晕头痛伴血压偏高', '根据您的症状描述，近期血压明显偏高（150/95mmHg），结合头晕、头痛表现，提示可能存在高血压。建议尽快至心血管内科就诊，完善诊室血压复测及必要的检查。', 'mid', '高血压', '心血管内科', 1);
