from datetime import datetime

from pydantic import BaseModel


class CallbackResult(BaseModel):
    """文档处理成功回调产物摘要。"""

    sectionsObjectKey: str | None = None
    structureObjectKey: str | None = None
    chunksObjectKey: str | None = None
    chunkCount: int = 0
    tokenCount: int = 0
    parserName: str | None = None
    embeddingModelCode: str | None = None


class ProcessCallback(BaseModel):
    """回调 system-service 的文档处理状态。"""

    taskId: int
    documentId: int
    stageCode: str
    status: str
    progress: int
    result: CallbackResult | None = None
    message: str | None = None
    errorCode: str | None = None
    errorMessage: str | None = None
    traceId: str | None = None
    finishedTime: datetime | None = None
