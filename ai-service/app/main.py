import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import diagnosis, knowledge
from app.core.config import get_settings

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
)
logger = logging.getLogger("health-ai")

settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        from app.core.vectorstore import get_vectorstore

        get_vectorstore()
        logger.info("向量库与嵌入模型预热完成")
    except Exception as exc:
        logger.warning("向量库预热失败（首次启动可能需下载模型）: %s", exc)
    yield


app = FastAPI(
    title=settings.app_name,
    version=settings.version,
    description="大健康行业智能问诊系统 AI 服务（RAG + 思维链推理）",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(diagnosis.router)
app.include_router(knowledge.router)


@app.get("/api/ai/health")
async def health():
    return {"status": "ok", "service": settings.app_name, "version": settings.version}
