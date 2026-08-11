import logging
from functools import lru_cache

from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma

from app.core.config import get_settings

logger = logging.getLogger(__name__)


@lru_cache(maxsize=1)
def get_embedding_model() -> HuggingFaceEmbeddings:
    settings = get_settings()
    logger.info("加载嵌入模型: %s", settings.embedding_model)
    return HuggingFaceEmbeddings(
        model_name=settings.embedding_model,
        encode_kwargs={"normalize_embeddings": True},
    )


@lru_cache(maxsize=1)
def get_vectorstore() -> Chroma:
    settings = get_settings()
    logger.info(
        "初始化向量库: collection=%s persist=%s",
        settings.collection_name,
        settings.chroma_persist_dir,
    )
    return Chroma(
        collection_name=settings.collection_name,
        embedding_function=get_embedding_model(),
        persist_directory=settings.chroma_persist_dir,
    )
