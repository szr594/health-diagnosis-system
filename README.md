# 大健康行业智能问诊系统

## 项目简介

大健康行业智能问诊系统是一个基于 **Java + Python 混合架构**的全栈项目（简历级），面向大健康行业提供 **AI 智能预问诊** 服务。用户通过描述症状，系统结合医疗知识库（RAG 检索增强生成）与思维链（CoT）推理，返回结构化的问诊建议：风险等级、疑似疾病方向、建议就诊科室、居家护理建议。

系统采用前后端分离架构（前端为规划中的 Vue3 界面），核心由两部分组成：

- **Java 后端（SpringBoot 3）**：负责用户认证（JWT）、问诊记录管理、知识库管理，并通过 HTTP 异步调用 Python AI 服务
- **Python AI 服务（FastAPI + LangChain）**：负责 RAG 向量检索、思维链推理、LLM 生成（DeepSeek），少样本学习（Few-shot）规范问诊话术

## 技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Java 17, SpringBoot 3.2, MyBatisPlus 3.5.5, Redis, JWT (Hutool), Hibernate Validator |
| AI服务 | Python 3.11, FastAPI, LangChain, ChromaDB, Sentence-Transformers (bge-small-zh) |
| LLM | DeepSeek（OpenAI 兼容接口，可切换任意兼容端点） |
| 前端（规划中） | Vue3 3.4, ElementPlus, Pinia, Vue Router, Axios |
| 数据库 | MySQL 8.0 |
| 部署 | Docker, Docker Compose |

## 系统架构

```
┌─────────────┐     ┌────────────────┐     ┌─────────────────┐
│   Vue3      │────▶│   SpringBoot   │────▶│     FastAPI     │
│  Frontend   │ JWT │   Backend      │HTTP │   AI Service    │
│  (规划中)   │     │    :8080       │     │    :8001        │
└─────────────┘     └────────────────┘     └─────────────────┘
                          │  │                      │
                    ┌─────┘  └──────┐        ┌──────┴────────┐
                    ▼               ▼        ▼               ▼
                ┌────────┐     ┌────────┐ ┌──────────┐  ┌──────────┐
                │  MySQL │     │  Redis │ │ ChromaDB │  │ DeepSeek │
                │ 业务数据│     │ 缓存/热 │ │ 医疗知识 │  │ LLM 推理 │
                │        │     │门问题  │ │ 向量库   │  │          │
                └────────┘     └────────┘ └──────────┘  └──────────┘
```

**核心链路（AI 预问诊）**：

```
用户描述症状
    │
    ▼
Java 后端（JWT 鉴权 → 校验参数 → Redis 缓存命中？）
    │ 未命中
    ▼
异步调用 Python FastAPI /api/ai/diagnosis（独立线程池，避免阻塞主线程）
    │
    ▼
Python：症状向量化 → ChromaDB 检索医疗知识 → CoT + Few-shot 提示词 → DeepSeek
    │
    ▼
结构化结果：风险等级 / 疑似疾病 / 建议科室 / 护理建议 / 免责声明
    │
    ▼
Java 后端保存问诊记录 → 写回 Redis 缓存 → 返回前端
```

## 核心功能

1. **用户/患者管理**：注册、登录、JWT 令牌鉴权、个人信息与健康档案（过敏史、既往病史）
2. **AI 智能预问诊**：症状描述 → RAG 知识检索 → 思维链（CoT）推理 → 结构化问诊建议；支持结果缓存与 AI 服务降级
3. **问诊记录管理**：问诊历史分页、详情查看、记录删除、热门问题排行（Redis ZSet）
4. **医疗知识库管理**：文档 CRUD、自动向量化入库（切块 → bge-small-zh 嵌入 → ChromaDB）、向量检索

## 目录结构

```
health-diagnosis-system/
├── backend/                      # Java SpringBoot 后端
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── resources/application.yml
│       └── java/com/health/diagnosis/
│           ├── HealthDiagnosisApplication.java
│           ├── config/           # Redis, MyBatisPlus, CORS, WebMvc, RestTemplate, 线程池
│           ├── interceptor/      # JWT 拦截器
│           ├── common/           # Result, PageResult, JwtUtil, BizException, 全局异常处理
│           ├── entity/           # User, ConsultationRecord, HealthKnowledge, ChatHistory
│           ├── dto/              # LoginDTO, ConsultationRequest, AiDiagnosisResponse...
│           ├── mapper/           # MyBatisPlus Mapper
│           ├── service/impl/     # 业务逻辑实现
│           └── controller/       # REST API 控制器
├── ai-service/                   # Python FastAPI AI 服务
│   ├── requirements.txt
│   ├── Dockerfile
│   ├── .env.example
│   └── app/
│       ├── main.py               # FastAPI 入口
│       ├── api/                  # 路由：diagnosis, knowledge
│       ├── core/                 # config, llm, vectorstore, prompt, chain
│       ├── services/             # diagnosis_service, knowledge_service
│       └── models/               # Pydantic 数据模型
├── database/
│   └── init.sql                  # MySQL 建库建表 + 种子数据
├── docker-compose.yml
└── .gitignore
```

## 快速启动

### 方式一：Docker Compose 一键部署

```bash
# 1. 配置 AI 服务环境变量（填入 DeepSeek API Key）
cp ai-service/.env.example ai-service/.env

# 2. 构建并启动全部服务（mysql / redis / ai-service / backend）
docker-compose up -d --build

# 3. 查看启动日志
docker-compose logs -f

# 4. 验证
curl http://localhost:8001/api/ai/health    # AI 服务健康检查
curl http://localhost:8080/api/user/login   # 后端登录接口
```

> 说明：ai-service 首次启动会自动下载中文嵌入模型 `BAAI/bge-small-zh-v1.5`（约 100MB），耗时取决于网络；若无法访问 HuggingFace，请在 `.env` 中设置 `HF_ENDPOINT=https://hf-mirror.com`。

### 方式二：本地开发启动

**1. 启动 MySQL 与 Redis**

```bash
docker-compose up -d mysql redis
# 初始化数据库（首次或重建时）
mysql -u root -p < database/init.sql
```

**2. 启动 AI 服务**

```bash
cd ai-service
pip install -r requirements.txt
cp .env.example .env          # 编辑填入 DEEPSEEK_API_KEY
uvicorn app.main:app --host 0.0.0.0 --port 8001
```

**3. 启动后端**

```bash
cd backend
mvn spring-boot:run           # 默认端口 8080
```

**4. 运行 AI 服务冒烟测试（可选）**

```bash
cd ai-service
python smoke_test.py          # 验证模块导入、路由注册、提示词渲染与容错解析
```

**默认账号**：`admin / 123456`（管理员）、`patient1 / 123456`（患者）

## API 文档

> 除登录/注册外，所有接口需在请求头携带 `Authorization: Bearer <token>`

### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/register | 用户注册 |
| POST | /api/user/login | 用户登录，返回 JWT |
| GET | /api/user/info | 获取当前用户信息 |

### 预问诊接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/medical/ai/pre-diagnosis | AI 预问诊（核心接口） |
| GET | /api/medical/consultation/list | 问诊记录分页 |
| GET | /api/medical/consultation/detail/{id} | 问诊记录详情 |
| DELETE | /api/medical/consultation/{id} | 删除问诊记录 |
| GET | /api/medical/hot | 热门问诊问题排行 |

### 知识库接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/knowledge/list | 知识文档分页（支持关键词） |
| GET | /api/knowledge/detail/{id} | 文档详情 |
| POST | /api/knowledge/create | 新增文档（自动向量化） |
| PUT | /api/knowledge/update/{id} | 更新文档（重新向量化） |
| DELETE | /api/knowledge/{id} | 删除文档（同步删向量） |
| POST | /api/knowledge/search | 向量检索 |

### AI 服务接口（Python）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/diagnosis | AI 预问诊（结构化） |
| POST | /api/ai/diagnosis/stream | AI 预问诊（SSE 流式） |
| POST | /api/ai/knowledge/upload | 文档向量化 |
| POST | /api/ai/knowledge/search | 向量检索 |
| POST | /api/ai/knowledge/delete | 删除向量 |
| GET | /api/ai/health | 健康检查 |

## Postman 测试用例

### 用例 1：用户登录

```
POST http://localhost:8080/api/user/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

// 响应 data.token 即为后续接口的 JWT 令牌
```

### 用例 2：AI 预问诊（核心）

```
POST http://localhost:8080/api/medical/ai/pre-diagnosis
Content-Type: application/json
Authorization: Bearer <token>

{
  "symptomDescription": "最近一周头晕头痛，测量血压偏高150/95，睡眠不好",
  "symptomDuration": "约一周",
  "age": 45,
  "gender": "male",
  "medicalHistory": "无",
  "allergyHistory": "无"
}

// 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3,
    "userId": 1,
    "nickname": "系统管理员",
    "symptomDescription": "最近一周头晕头痛，测量血压偏高150/95，睡眠不好",
    "aiAdvice": "您描述的头痛伴血压明显偏高...",
    "riskLevel": "mid",
    "possibleDiseases": "高血压,紧张性头痛,偏头痛",
    "suggestedDepartment": "心血管内科",
    "status": 1
  }
}
```

### 用例 3：知识库新增（向量化）

```
POST http://localhost:8080/api/knowledge/create
Content-Type: application/json
Authorization: Bearer <token>

{
  "title": "头痛的家庭处理",
  "category": "神经内科",
  "content": "头痛是常见症状，紧张性头痛多为双侧压迫感；偏头痛多伴恶心畏光...",
  "source": "内部知识库"
}
```

### 用例 4：向量检索

```
POST http://localhost:8080/api/knowledge/search
Content-Type: application/json
Authorization: Bearer <token>

{
  "query": "头痛怎么办",
  "topK": 4
}
```

### 用例 5：AI 服务健康检查

```
GET http://localhost:8001/api/ai/health
```

## 异常处理与容错设计

1. **参数校验**：Hibernate Validator（`@NotBlank` / `@Size` / `@Pattern`），失败返回 400 与首个错误信息
2. **全局异常**：`@RestControllerAdvice` 统一捕获业务异常与未知异常，堆栈不泄露给前端
3. **AI 调用降级**：Python 接口超时（60s）或异常时，后端自动降级为友好提示并标记记录失败原因，保证接口可用
4. **缓存兜底**：相同症状的预问诊结果缓存到 Redis（TTL 10 分钟），命中时直接返回，减少 AI 调用成本
5. **LLM 幻觉控制**：AI 侧使用 Few-shot 示例约束输出结构，Pydantic 结构化解析失败时自动重试原始输出

## 部署注意事项（导师点评）

- **安全**：JWT 密钥务必通过环境变量注入；生产环境为密码启用 BCrypt 加盐；敏感接口建议增加登录次数限流
- **性能**：AI 调用走独立线程池；高并发场景建议引入消息队列（RabbitMQ）异步解耦预问诊，替代当前同步等待模式
- **模型**：嵌入模型已本地化，无需在线调用；DeepSeek 通过 OpenAI 兼容接口接入，可无缝切换其它大模型
- **可观测**：建议补充 AI 服务调用链追踪（耗时、token 消耗、检索命中率）与日志告警

## 后续规划

- [ ] Vue3 + ElementPlus 前端界面（登录、预问诊、问诊记录、知识库管理）
- [ ] 预问诊 SSE 流式输出接入前端
- [ ] 引入 RabbitMQ 异步解耦 + 问诊结果回调
- [ ] 医生端接诊与人工复核流程
