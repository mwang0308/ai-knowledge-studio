from __future__ import annotations

from io import BytesIO
from urllib.parse import urlparse

from minio import Minio

from app.config import settings


class MinioDocumentClient:
    """MinIO 文档产物客户端。"""

    def __init__(self) -> None:
        endpoint = settings.minio_endpoint
        parsed = urlparse(endpoint)
        secure = parsed.scheme == "https"
        netloc = parsed.netloc if parsed.netloc else endpoint
        self._client = Minio(
            netloc,
            access_key=settings.minio_access_key,
            secret_key=settings.minio_secret_key,
            secure=secure,
        )

    def download_bytes(self, bucket_name: str, object_key: str) -> bytes:
        response = self._client.get_object(bucket_name, object_key)
        try:
            return response.read()
        finally:
            response.close()
            response.release_conn()

    def upload_text(self, bucket_name: str, object_key: str, content: str, content_type: str) -> None:
        data = content.encode("utf-8")
        self._client.put_object(
            bucket_name,
            object_key,
            BytesIO(data),
            length=len(data),
            content_type=content_type,
        )
