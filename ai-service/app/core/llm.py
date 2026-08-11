import logging
from functools import lru_cache

from langchain_openai import ChatOpenAI

from app.core.config import get_settings

logger = logging.getLogger(__name__)


@lru_cache(maxsize=1)
def get_llm() -> ChatOpenAI:
    settings = get_settings()
    logger.info("初始化 LLM: model=%s base_url=%s", settings.llm_model, settings.llm_base_url)
    return ChatOpenAI(
        model=settings.llm_model,
        api_key=settings.deepseek_api_key,
        base_url=settings.llm_base_url,
        temperature=settings.temperature,
        max_tokens=settings.max_tokens,
        timeout=settings.llm_timeout,
        max_retries=2,
    )
