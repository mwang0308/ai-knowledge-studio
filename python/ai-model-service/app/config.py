from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """模型网关服务配置。"""

    service_name: str = "ai-model-service"
    log_level: str = "INFO"

    model_config = SettingsConfigDict(env_prefix="AI_MODEL_", env_file=".env")


settings = Settings()
