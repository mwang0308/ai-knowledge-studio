from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """文档处理服务配置。"""

    service_name: str = "document-process-service"
    log_level: str = "INFO"
    rabbitmq_host: str = "localhost"
    rabbitmq_port: int = 5672
    rabbitmq_username: str = "admin"
    rabbitmq_password: str = "admin"
    rabbitmq_queue: str = "knowledge.document.parse-chunk.queue"
    minio_endpoint: str = "http://localhost:9900"
    minio_access_key: str = "minio"
    minio_secret_key: str = "minio1234"
    minio_bucket_name: str = "rag-doc"
    chunk_max_chars: int = 1200
    chunk_overlap_chars: int = 120
    table_chunk_rows: int = 30
    pdf_parse_block_pages: int = 10
    enable_mineru_ocr: bool = True
    mineru_ocr_adapter: str | None = "app.infrastructure.ocr.mineru_adapter:recognize_pdf_images"
    mineru_command: str = "mineru"
    mineru_backend: str = "pipeline"
    mineru_method: str = "ocr"
    mineru_lang: str = "ch"
    mineru_timeout_seconds: int = 300

    model_config = SettingsConfigDict(env_prefix="DOCUMENT_PROCESS_", env_file=".env")


settings = Settings()
