from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """AI 编排服务配置。"""

    service_name: str = "ai-agent-service"
    log_level: str = "INFO"

    model_config = SettingsConfigDict(env_prefix="AI_AGENT_", env_file=".env")


settings = Settings()
