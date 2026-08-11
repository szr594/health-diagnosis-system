import json
import logging
import re

from langchain_core.prompts import ChatPromptTemplate

from app.core import chain as chain_builder
from app.core.config import get_settings
from app.core.llm import get_llm
from app.core.prompt import (
    MULTI_TURN_HUMAN_TEMPLATE,
    MULTI_TURN_SYSTEM_PROMPT,
    STREAM_HUMAN_TEMPLATE,
    STREAM_MULTI_TURN_HUMAN_TEMPLATE,
    STREAM_SYSTEM_PROMPT,
    get_gender_text,
)
from app.core.vectorstore import get_vectorstore
from app.models.diagnosis import DiagnosisRequest, DiagnosisResult

logger = logging.getLogger(__name__)


def _retrieve_context(symptom: str, top_k: int) -> str:
    try:
        vs = get_vectorstore()
        docs = vs.similarity_search(symptom, k=top_k)
        if not docs:
            return "知识库暂无与本次症状直接相关的条目，请结合自身医学常识作答。"
        lines = [f"[{i + 1}] {doc.page_content}" for i, doc in enumerate(docs)]
        return "\n\n".join(lines)
    except Exception as exc:
        logger.warning("向量检索失败，降级为无上下文模式: %s", exc)
        return "知识库检索暂不可用，请结合自身医学常识作答。"


def _is_multi_turn(req: DiagnosisRequest) -> bool:
    return bool(req.conversation_history and req.conversation_history.strip())


def _extract_json(text: str) -> dict:
    cleaned = text.strip()
    cleaned = re.sub(r"^```(?:json)?\s*", "", cleaned)
    cleaned = re.sub(r"\s*```$", "", cleaned)
    start, end = cleaned.find("{"), cleaned.rfind("}")
    if start == -1 or end == -1 or end <= start:
        raise ValueError("输出中未找到合法 JSON 对象")
    return json.loads(cleaned[start:end + 1])


def _fallback_result(req: DiagnosisRequest) -> DiagnosisResult:
    return DiagnosisResult(
        risk_level="mid",
        possible_diseases=["资料不足，暂无法给出疑似判断", "建议专科就诊进一步排查"],
        suggested_department="全科医学科",
        advice=(
            "非常抱歉，本次 AI 预问诊服务暂时无法正常生成建议。"
            f"根据您描述的「{req.symptom_description[:80]}」，"
            "建议您密切观察症状变化；若症状持续或加重，请及时前往全科医学科"
            "或相关专科就诊，由执业医师为您进一步评估。"
        ),
        disclaimer="本建议由 AI 生成，仅供参考，不能替代执业医师诊断。",
    )


def generate_diagnosis(req: DiagnosisRequest) -> DiagnosisResult:
    settings = get_settings()
    context = _retrieve_context(req.symptom_description, settings.top_k)

    if _is_multi_turn(req):
        return _generate_multi_turn(req, context)
    return _generate_single_turn(req, context)


def _generate_single_turn(req: DiagnosisRequest, context: str) -> DiagnosisResult:
    vars = chain_builder.get_invoke_variables(
        symptom=req.symptom_description,
        context=context,
        age=str(req.age) if req.age is not None else "未知",
        gender=get_gender_text(req.gender),
        medical_history=req.medical_history or "无",
        allergy_history=req.allergy_history or "无",
    )

    try:
        return chain_builder.get_chain().invoke(vars)
    except Exception as exc:
        logger.warning("结构化链路失败，尝试降级原始输出: %s", exc)

    try:
        llm = get_llm()
        raw = llm.invoke(vars["symptom"] + "\n\n请参考以下知识库内容作答：\n" + context)
        payload = _extract_json(raw.content if hasattr(raw, "content") else str(raw))
        return DiagnosisResult.model_validate(payload)
    except Exception as exc:
        logger.error("降级解析仍失败: %s", exc)

    if get_settings().enable_fallback:
        logger.warning("返回兜底建议")
        return _fallback_result(req)
    raise RuntimeError("AI 服务生成建议失败")


def _generate_multi_turn(req: DiagnosisRequest, context: str) -> DiagnosisResult:
    settings = get_settings()

    history = req.conversation_history or ""
    summary = req.conversation_summary or "暂无摘要"

    prompt = ChatPromptTemplate.from_messages([
        ("system", MULTI_TURN_SYSTEM_PROMPT),
        ("human", MULTI_TURN_HUMAN_TEMPLATE),
    ])

    messages = prompt.format_messages(
        examples="",
        summary=summary,
        history=history,
        age=str(req.age) if req.age is not None else "未知",
        gender=get_gender_text(req.gender),
        medical_history=req.medical_history or "无",
        allergy_history=req.allergy_history or "无",
        symptom=req.symptom_description,
        context=context,
        format_instructions="请按【输出结构】以 JSON 格式输出诊断结果。",
    )

    try:
        llm = get_llm()
        raw = llm.invoke(messages)
        text = raw.content if hasattr(raw, "content") else str(raw)
        payload = _extract_json(text)
        return DiagnosisResult.model_validate(payload)
    except Exception as exc:
        logger.warning("多轮问诊解析失败: %s", exc)

    try:
        llm = get_llm()
        raw = llm.invoke(messages)
        text = raw.content if hasattr(raw, "content") else str(raw)
        return DiagnosisResult(
            risk_level="mid",
            possible_diseases=["需进一步分析"],
            suggested_department="全科医学科",
            advice=text[:1000],
            disclaimer="本建议由 AI 生成，仅供参考，不能替代执业医师诊断。",
        )
    except Exception as exc2:
        logger.error("多轮问诊完全失败: %s", exc2)
        if settings.enable_fallback:
            return _fallback_result(req)
        raise RuntimeError("AI 服务生成建议失败") from exc2


def stream_diagnosis(req: DiagnosisRequest):
    settings = get_settings()
    context = _retrieve_context(req.symptom_description, settings.top_k)

    is_multi = _is_multi_turn(req)

    system_prompt = STREAM_SYSTEM_PROMPT
    if is_multi:
        pt = STREAM_MULTI_TURN_HUMAN_TEMPLATE
        history = req.conversation_history or ""
        summary = req.conversation_summary or "暂无摘要"
        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            ("human", pt),
        ])
        messages = prompt.format_messages(
            summary=summary,
            history=history,
            age=str(req.age) if req.age is not None else "未知",
            gender=get_gender_text(req.gender),
            medical_history=req.medical_history or "无",
            allergy_history=req.allergy_history or "无",
            symptom=req.symptom_description,
            context=context,
        )
    else:
        pt = STREAM_HUMAN_TEMPLATE
        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            ("human", pt),
        ])
        messages = prompt.format_messages(
            age=str(req.age) if req.age is not None else "未知",
            gender=get_gender_text(req.gender),
            medical_history=req.medical_history or "无",
            allergy_history=req.allergy_history or "无",
            symptom=req.symptom_description,
            context=context,
        )

    llm = get_llm()
    for chunk in llm.stream(messages):
        content = getattr(chunk, "content", None)
        if isinstance(content, str) and content:
            yield content
