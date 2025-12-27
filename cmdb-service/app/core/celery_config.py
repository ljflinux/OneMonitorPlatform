from celery import Celery
from app.core.config import settings

# 创建Celery实例
celery = Celery(
    "cmdb_service",
    broker=settings.REDIS_URL,
    backend=settings.REDIS_URL,
    include=[
        "app.tasks.ci_tasks",
        "app.tasks.relation_tasks",
        "app.tasks.change_history_tasks"
    ]
)

# 配置Celery
celery.conf.update(
    result_expires=3600,
    task_serializer="json",
    result_serializer="json",
    accept_content=["json"],
    timezone="Asia/Shanghai",
    enable_utc=True,
    broker_connection_retry_on_startup=True,
)

# 创建任务目录
import os
os.makedirs(os.path.join(os.path.dirname(__file__), "..", "tasks"), exist_ok=True)

if __name__ == "__main__":
    celery.start()