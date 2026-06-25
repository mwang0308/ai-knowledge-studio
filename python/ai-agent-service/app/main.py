from fastapi import FastAPI

from app.api.health import router as health_router
from app.config import settings


def create_app() -> FastAPI:
    """创建 FastAPI 应用实例，后续召回测试和 RAG 编排能力在这里统一装配。"""
    app = FastAPI(title=settings.service_name, version="0.1.0")
    app.include_router(health_router)
    return app


app = create_app()
