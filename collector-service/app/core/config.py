from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    # Basic settings
    PROJECT_NAME: str = "Collector Service"
    API_V1_STR: str = "/api/v1"
    VERSION: str = "1.0.0"
    
    # Database settings
    DATABASE_URL: str = "postgresql://admin:admin123@postgres:5432/onemonitor"
    
    # Redis settings
    REDIS_URL: str = "redis://redis:6379/0"
    
    # OpenTelemetry settings
    OTEL_EXPORTER_OTLP_ENDPOINT: str = "http://otel-collector:4317"
    OTEL_SERVICE_NAME: str = "collector-service"
    OTEL_TRACES_EXPORTER: str = "otlp"
    OTEL_METRICS_EXPORTER: str = "otlp"
    OTEL_LOGS_EXPORTER: str = "otlp"
    
    # Celery settings
    CELERY_BROKER_URL: str = "redis://redis:6379/1"
    CELERY_RESULT_BACKEND: str = "redis://redis:6379/2"
    
    # CMDB Service
    CMDB_SERVICE_URL: str = "http://cmdb-service:8000/api/v1"
    
    # Prometheus settings
    PROMETHEUS_PORT: int = 8001
    
    # Auth settings
    SECRET_KEY: str = "your-secret-key-here"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30

    class Config:
        env_file = ".env"


settings = Settings()