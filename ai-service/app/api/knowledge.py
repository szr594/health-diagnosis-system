import logging

from fastapi import APIRouter, HTTPException

from app.models.knowledge import (
    KnowledgeDeleteRequest,
    KnowledgeSearchRequest,
    KnowledgeUploadRequest,
)
from app.services import knowledge_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/ai", tags=["知识库"])


@router.post("/knowledge/upload")
async def upload_knowledge(req: KnowledgeUploadRequest):
    try:
        result = knowledge_service.embed_and_upload(
            doc_id=req.id, title=req.title, category=req.category, content=req.content
        )
        return {"code": 200, "message": "success", "data": result}
    except Exception as exc:
        logger.exception("知识向量化失败")
        raise HTTPException(status_code=500, detail=f"知识向量化失败: {exc}") from exc


@router.post("/knowledge/search")
async def search_knowledge(req: KnowledgeSearchRequest):
    try:
        items = knowledge_service.search(req.query, req.top_k)
        return {"code": 200, "message": "success", "data": items}
    except Exception as exc:
        logger.exception("知识检索失败")
        raise HTTPException(status_code=500, detail=f"知识检索失败: {exc}") from exc


@router.post("/knowledge/delete")
async def delete_knowledge(req: KnowledgeDeleteRequest):
    try:
        result = knowledge_service.delete_doc(req.id)
        return {"code": 200, "message": "success", "data": result}
    except Exception as exc:
        logger.exception("向量删除失败")
        raise HTTPException(status_code=500, detail=f"向量删除失败: {exc}") from exc
