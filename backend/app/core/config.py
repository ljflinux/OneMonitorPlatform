from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    # 项目基本信息
    PROJECT_NAME: str = "OneMonitor"
    PROJECT_VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"
    FRONTEND_URL: str = "http://localhost:5173"

    # 数据库配置
    DATABASE_URL: str = "postgresql://admin:admin123@localhost:5432/onemonitor"
    SQLALCHEMY_ECHO: bool = False

    # Redis配置
    REDIS_URL: str = "redis://localhost:6379/0"

    # JWT配置
    SECRET_KEY: str = "your-secret-key-change-me-in-production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    # 日志配置
    LOG_LEVEL: str = "INFO"
    LOG_FILE: str = "app.log"

    # 监控配置
    PROMETHEUS_URL: str = "http://localhost:9090"
    GRAFANA_URL: str = "http://localhost:3000"

    # 告警配置
    ALERTMANAGER_URL: str = "http://localhost:9093"
    EMAIL_SMTP_SERVER: str = "smtp.example.com"
    EMAIL_SMTP_PORT: int = 587
    EMAIL_USERNAME: str = "alert@onemonitor.io"
    EMAIL_PASSWORD: str = "your-email-password"
    EMAIL_FROM: str = "alert@onemonitor.io"

    # CORS配置
    BACKEND_CORS_ORIGINS: List[str] = ["http://localhost:5173"]

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()