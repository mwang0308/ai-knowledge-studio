from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """文档处理服务配置。"""

    service_name: str = "document-process-service"
    log_level: str = "INFO"

    model_config = SettingsConfigDict(env_prefix="DOCUMENT_PROCESS_", env_file=".env")


settings = Settings()
