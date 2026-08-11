from functools import lru_cache

from langchain_core.output_parsers import PydanticOutputParser
from langchain_core.prompts import ChatPromptTemplate

from app.core.llm import get_llm
from app.core.prompt import HUMAN_TEMPLATE, SYSTEM_PROMPT, build_few_shot_text
from app.models.diagnosis import DiagnosisResult

_parser = PydanticOutputParser(pydantic_object=DiagnosisResult)


@lru_cache(maxsize=1)
def get_prompt() -> ChatPromptTemplate:
    return ChatPromptTemplate.from_messages(
        [
            ("system", SYSTEM_PROMPT),
            ("human", HUMAN_TEMPLATE),
        ]
    )


@lru_cache(maxsize=1)
def get_chain():
    return get_prompt() | get_llm() | _parser


def get_parser() -> PydanticOutputParser:
    return _parser


def get_invoke_variables(
    symptom: str,
    context: str,
    age: str = "未知",
    gender: str = "未知",
    medical_history: str = "无",
    allergy_history: str = "无",
) -> dict:
    return {
        "examples": build_few_shot_text(),
        "age": age,
        "gender": gender,
        "medical_history": medical_history or "无",
        "allergy_history": allergy_history or "无",
        "symptom": symptom,
        "context": context,
        "format_instructions": _parser.get_format_instructions(),
    }
