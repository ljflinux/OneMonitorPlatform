from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    # 项目基本信息
    PROJECT_NAME: str = "CMDB Service"
    PROJECT_VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"
    FRONTEND_URL: str = "http://localhost:5173"

    # 数据库配置
    DATABASE_URL: str = "postgresql://admin:admin123@localhost:5432/onemonitor"
    SQLALCHEMY_ECHO: bool = False

    # Redis配置
    REDIS_URL: str = "redis://localhost:6379/0"

    # 日志配置
    LOG_LEVEL: str = "INFO"
    LOG_FILE: str = "cmdb-service.log"

    # CORS配置
    BACKEND_CORS_ORIGINS: List[str] = ["http://localhost:5173"]

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()