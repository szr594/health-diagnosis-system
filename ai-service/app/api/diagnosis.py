import json
import logging

from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse

from app.models.diagnosis import DiagnosisRequest, DiagnosisResponse
from app.services import diagnosis_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/ai", tags=["预问诊"])


@router.post("/diagnosis", response_model=DiagnosisResponse)
async def diagnosis(req: DiagnosisRequest):
    try:
        result = diagnosis_service.generate_diagnosis(req)
        return DiagnosisResponse(data=result)
    except Exception as exc:
        logger.exception("预问诊处理异常")
        raise HTTPException(status_code=500, detail=f"AI 服务处理异常: {exc}") from exc


@router.post("/diagnosis/stream")
async def diagnosis_stream(req: DiagnosisRequest):

    def event_generator():
        try:
            meta = json.dumps({
                "type": "meta",
                "session_id": req.session_id or "",
            }, ensure_ascii=False)
            yield f"data: {meta}\n\n"

            for token in diagnosis_service.stream_diagnosis(req):
                payload = json.dumps({"type": "token", "content": token}, ensure_ascii=False)
                yield f"data: {payload}\n\n"
            yield "data: [DONE]\n\n"
        except Exception as exc:
            logger.exception("流式输出异常")
            yield f'data: {json.dumps({"type": "error", "error": str(exc)}, ensure_ascii=False)}\n\n'
            yield "data: [DONE]\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )
