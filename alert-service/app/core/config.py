from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    # Basic settings
    PROJECT_NAME: str = "Alert Service"
    API_V1_STR: str = "/api/v1"
    VERSION: str = "1.0.0"
    
    # Database settings
    DATABASE_URL: str = "postgresql://admin:admin123@postgres:5432/onemonitor"
    
    # Redis settings
    REDIS_URL: str = "redis://redis:6379/0"
    
    # Celery settings
    CELERY_BROKER_URL: str = "redis://redis:6379/1"
    CELERY_RESULT_BACKEND: str = "redis://redis:6379/2"
    
    # Service URLs
    CMDB_SERVICE_URL: str = "http://cmdb-service:8000/api/v1"
    COLLECTOR_SERVICE_URL: str = "http://collector-service:8000/api/v1"
    NOTIFICATION_SERVICE_URL: str = "http://notification-service:8000/api/v1"
    
    # Auth settings
    SECRET_KEY: str = "your-secret-key-here"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    
    # Alert settings
    ALERT_THRESHOLD_DEFAULT: int = 100
    ALERT_RETRY_COUNT_DEFAULT: int = 3
    ALERT_SILENCE_DURATION_DEFAULT: int = 3600  # 1 hour
    
    # Prometheus and Alertmanager settings
    PROMETHEUS_URL: str = "http://prometheus:9090"
    ALERTMANAGER_URL: str = "http://alertmanager:9093"
    
    class Config:
        env_file = ".env"


settings = Settings()
