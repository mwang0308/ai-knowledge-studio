from pydantic import BaseModel, ConfigDict


class DocumentProcessMessage(BaseModel):
    """Java 投递给文档处理服务的任务消息。"""

    model_config = ConfigDict(populate_by_name=True)

    messageId: str
    taskId: int
    taskNo: str | None = None
    stageCode: str
    knowledgeBaseId: int
    directoryId: int
    documentId: int
    versionId: int
    fileResourceId: int
    fileName: str
    fileType: str
    bucketName: str
    objectKey: str
    chunkConfigSnapshot: str | None = None
    callbackUrl: str
    retryCount: int = 0
    traceId: str | None = None
    createdTime: str | None = None
