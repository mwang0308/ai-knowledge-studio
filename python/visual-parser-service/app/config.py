from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """视觉解析服务配置。"""

    service_name: str = "visual-parser-service"
    log_level: str = "INFO"

    model_config = SettingsConfigDict(env_prefix="VISUAL_PARSER_", env_file=".env")


settings = Settings()
