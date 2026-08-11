from typing import List, Optional

from pydantic import BaseModel, Field


class DiagnosisRequest(BaseModel):

    symptom_description: str = Field(
        ..., min_length=2, max_length=2000, description="症状描述（必填）"
    )
    symptom_duration: Optional[str] = Field(
        None, max_length=50, description="症状持续时间，如：约一周"
    )
    age: Optional[int] = Field(None, ge=0, le=130, description="年龄")
    gender: Optional[str] = Field(
        None, description="性别：male / female / unknown"
    )
    medical_history: Optional[str] = Field(
        None, max_length=500, description="既往病史"
    )
    allergy_history: Optional[str] = Field(
        None, max_length=500, description="过敏史"
    )

    session_id: Optional[str] = Field(
        None, description="会话唯一标识（UUID），由 Java 后端传入"
    )
    conversation_history: Optional[str] = Field(
        None, max_length=3000, description="最近 N 轮对话历史文本（用户 + AI），由 Java 后端拼接"
    )
    conversation_summary: Optional[str] = Field(
        None, max_length=1000, description="长对话的 AI 压缩摘要（token 预算控制用）"
    )


class DiagnosisResult(BaseModel):

    risk_level: str = Field(..., description="风险等级：low低 / mid中 / high高")
    possible_diseases: List[str] = Field(
        ..., description="疑似疾病方向列表（2~4 个可能性）"
    )
    suggested_department: str = Field(..., description="建议就诊科室")
    advice: str = Field(..., description="综合问诊建议（分条可执行）")
    disclaimer: str = Field(
        ..., description="免责声明，必须提示仅供参考、不能替代执业医师诊断"
    )


class DiagnosisResponse(BaseModel):

    code: int = 200
    message: str = "success"
    data: Optional[DiagnosisResult] = None
