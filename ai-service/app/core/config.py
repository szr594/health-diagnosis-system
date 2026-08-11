import os
from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict

os.environ.setdefault("HF_ENDPOINT", os.getenv("HF_ENDPOINT", "https://hf-mirror.com"))
os.environ.setdefault("HF_HUB_DISABLE_TELEMETRY", "1")


class Settings(BaseSettings):

    app_name: str = "大健康智能问诊 AI 服务"
    version: str = "1.0.0"
    debug: bool = True
    host: str = "0.0.0.0"
    port: int = 8001

    deepseek_api_key: str = ""
    llm_base_url: str = "https://api.deepseek.com/v1"
    llm_model: str = "deepseek-chat"
    temperature: float = 0.3
    llm_timeout: int = 60
    max_tokens: int = 1024

    embedding_model: str = "BAAI/bge-small-zh-v1.5"

    chroma_persist_dir: str = "./data/chroma"
    collection_name: str = "health_knowledge"
    chunk_size: int = 500
    chunk_overlap: int = 50
    top_k: int = 4

    enable_fallback: bool = True

    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
