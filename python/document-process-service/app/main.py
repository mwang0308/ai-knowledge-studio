import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.health import router as health_router
from app.config import settings
from app.application.parse_chunk_pipeline import ParseChunkPipeline
from app.infrastructure.callback.system_service_client import SystemServiceCallbackClient
from app.infrastructure.mq.consumer import DocumentProcessConsumer
from app.infrastructure.storage.minio_client import MinioDocumentClient


@asynccontextmanager
async def lifespan(app: FastAPI):
    """启动文档处理 MQ 消费线程。"""
    logging.basicConfig(level=settings.log_level)
    storage_client = MinioDocumentClient()
    pipeline = ParseChunkPipeline(storage_client)
    callback_client = SystemServiceCallbackClient()
    consumer = DocumentProcessConsumer(pipeline, callback_client)
    consumer.start_background()
    yield


def create_app() -> FastAPI:
    """创建 FastAPI 应用实例，后续文档处理能力在这里统一装配。"""
    app = FastAPI(title=settings.service_name, version="0.1.0", lifespan=lifespan)
    app.include_router(health_router)
    return app


app = create_app()
