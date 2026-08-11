"""
AI 服务冒烟测试：桩掉重型依赖（torch/chromadb），验证：
1. 所有模块可正常导入，无语法/命名错误
2. FastAPI 应用可构建、路由正确注册
3. 提示词模板可正确渲染（CoT + Few-shot）
4. 容错 JSON 解析逻辑正确
5. 配置读取正确

运行：python smoke_test.py
"""
import json
import sys
import types

# ---------------------------------------------------------------------------
# 预置重型依赖桩，避免加载 torch / sentence-transformers / chromadb
# ---------------------------------------------------------------------------
for _name in ("sentence_transformers", "chromadb", "torch", "chromadb.api",
              "chromadb.api.client", "chromadb.config"):
    sys.modules.setdefault(_name, types.ModuleType(_name))

lc = types.ModuleType("langchain_community")
emb = types.ModuleType("langchain_community.embeddings")
hug = types.ModuleType("langchain_community.embeddings.huggingface")


class _HuggingFaceEmbeddings:
    def __init__(self, *args, **kwargs):
        pass


emb.HuggingFaceEmbeddings = _HuggingFaceEmbeddings

vs = types.ModuleType("langchain_community.vectorstores")


class _Chroma:
    def __init__(self, *args, **kwargs):
        pass


vs.Chroma = _Chroma

sys.modules["langchain_community"] = lc
sys.modules["langchain_community.embeddings"] = emb
sys.modules["langchain_community.embeddings.huggingface"] = hug
sys.modules["langchain_community.vectorstores"] = vs

# 设置测试用环境变量（避免真实 Key 缺失导致配置异常）
import os
os.environ.setdefault("DEEPSEEK_API_KEY", "sk-test-key")
os.environ.setdefault("CHROMA_PERSIST_DIR", "./data/chroma-test")

passed = 0
failed = 0


def check(name: str, cond: bool):
    global passed, failed
    if cond:
        passed += 1
        print(f"  [PASS] {name}")
    else:
        failed += 1
        print(f"  [FAIL] {name}")


print("== 1. 模块导入 ==")
from app.core.config import get_settings                  # noqa: E402
from app.core.prompt import build_few_shot_text, SYSTEM_PROMPT, STREAM_SYSTEM_PROMPT  # noqa: E402
from app.core.chain import get_prompt, get_parser, get_invoke_variables  # noqa: E402
from app.models.diagnosis import DiagnosisRequest, DiagnosisResult  # noqa: E402
from app.services.diagnosis_service import _extract_json, _fallback_result, get_gender_text  # noqa: E402
check("core + models + services 模块导入", True)

print("== 2. FastAPI 应用构建与路由 ==")
from app.main import app  # noqa: E402
paths = sorted(app.openapi()["paths"].keys())
expected = [
    "/api/ai/diagnosis",
    "/api/ai/diagnosis/stream",
    "/api/ai/knowledge/upload",
    "/api/ai/knowledge/search",
    "/api/ai/knowledge/delete",
    "/api/ai/health",
]
for p in expected:
    check(f"路由注册 {p}", p in paths)
check("FastAPI 应用标题", app.title == "大健康智能问诊 AI 服务")

print("== 3. 配置读取 ==")
settings = get_settings()
check("DeepSeek 模型", settings.llm_model == "deepseek-chat")
check("嵌入模型", settings.embedding_model == "BAAI/bge-small-zh-v1.5")
check("base_url", settings.llm_base_url == "https://api.deepseek.com/v1")

print("== 4. 提示词渲染（CoT + Few-shot） ==")
variables = get_invoke_variables(
    symptom="头痛头晕一周",
    context="高血压相关医学知识...",
    age="45",
    gender="男",
)
prompt = get_prompt()
messages = prompt.format_messages(**variables)
text = messages[0].content + "\n" + messages[1].content
check("系统提示词含思维链", "推理步骤" in SYSTEM_PROMPT)
check("系统提示词含核心原则", "绝不给出确定性诊断结论" in SYSTEM_PROMPT)
check("Few-shot 示例渲染", "示例1" in text and "示例2" in text)
check("患者信息渲染", "年龄：45 岁" in text)
check("症状渲染", "头痛头晕一周" in text)
check("知识库上下文渲染", "高血压相关医学知识" in text)
check("格式约束注入", "risk_level" in messages[1].content)
check("流式提示词存在", "分条输出预问诊建议" in STREAM_SYSTEM_PROMPT)

print("== 5. 容错 JSON 解析 ==")
sample = '```json\n{"risk_level": "low", "possible_diseases": ["a"], "suggested_department": "x", "advice": "y", "disclaimer": "z"}\n```'
check("markdown 代码块剥离", _extract_json(sample)["risk_level"] == "low")
sample2 = '思考中... {"risk_level": "mid"} 结尾内容'
check("前后噪声剥离", _extract_json(sample2)["risk_level"] == "mid")
try:
    _extract_json("no json here")
    check("非法输入抛异常", False)
except ValueError:
    check("非法输入抛异常", True)

print("== 6. 结构化模型校验 ==")
result = DiagnosisResult.model_validate(
    json.loads(sample.lstrip("```json\n").rstrip("\n```"))
)
check("Pydantic 模型解析", result.risk_level == "low")
check("性别映射", get_gender_text("male") == "男")
check("性别映射默认", get_gender_text(None) == "未知")

print("== 7. 兜底结果 ==")
fb = _fallback_result(DiagnosisRequest(symptom_description="头痛"))
check("兜底风险等级", fb.risk_level == "mid")
check("兜底含免责声明", "不能替代执业医师诊断" in fb.disclaimer)
check("兜底引用症状", "头痛" in fb.advice)

print("== 8. 流式/同步入参校验 ==")
req = DiagnosisRequest(symptom_description="咳嗽两周", age=30)
check("可选字段默认", req.symptom_duration is None and req.gender is None)

print(f"\n结果: {passed} passed, {failed} failed")
sys.exit(0 if failed == 0 else 1)
