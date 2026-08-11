import logging
from typing import List

from langchain_text_splitters import RecursiveCharacterTextSplitter

from app.core.config import get_settings
from app.core.vectorstore import get_vectorstore
from app.models.knowledge import KnowledgeItem

logger = logging.getLogger(__name__)


def _get_splitter() -> RecursiveCharacterTextSplitter:
    settings = get_settings()
    return RecursiveCharacterTextSplitter(
        chunk_size=settings.chunk_size,
        chunk_overlap=settings.chunk_overlap,
        separators=["\n\n", "\n", "。", "；", "！", "？", "，", " "],
    )


def embed_and_upload(doc_id: int, title: str, category: str, content: str) -> dict:
    splitter = _get_splitter()
    chunks = splitter.split_text(content)
    if not chunks:
        return {"doc_id": doc_id, "chunk_count": 0}

    meta = [
        {
            "doc_id": str(doc_id),
            "title": title,
            "category": category or "",
            "chunk_index": i,
        }
        for i in range(len(chunks))
    ]
    vs = get_vectorstore()
    delete_doc(doc_id)
    vs.add_texts(texts=chunks, metadatas=meta)
    logger.info("文档向量化成功: doc_id=%s chunks=%s", doc_id, len(chunks))
    return {"doc_id": doc_id, "chunk_count": len(chunks)}


def search(query: str, top_k: int) -> List[KnowledgeItem]:
    vs = get_vectorstore()
    hits = vs.similarity_search_with_score(query, k=top_k)
    items = []
    for doc, score in hits:
        items.append(
            KnowledgeItem(
                doc_id=int(doc.metadata.get("doc_id", 0)),
                title=doc.metadata.get("title", ""),
                category=doc.metadata.get("category", ""),
                content=doc.page_content,
                score=round(float(score), 4),
            )
        )
    return items


def delete_doc(doc_id: int) -> dict:
    vs = get_vectorstore()
    try:
        vs.delete(where={"doc_id": str(doc_id)})
        logger.info("向量删除成功: doc_id=%s", doc_id)
    except Exception:
        logger.warning("向量删除失败: doc_id=%s", doc_id)
    return {"doc_id": doc_id, "deleted": True}
