from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import Dict

from app.core.config import settings
from app.db.session import get_db
from app.api.v1.endpoints import alert

# 创建FastAPI应用
app = FastAPI(
    title="OneMonitor Alert Service",
    description="一体化运维监控平台 - 告警服务",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(
    alert.router,
    prefix="/api/v1/alerts",
    tags=["alerts"],
    dependencies=[Depends(get_db)]
)


# 健康检查端点
@app.get("/health", response_model=Dict[str, str])
def health_check(db: Session = Depends(get_db)):
    try:
        # 测试数据库连接
        db.execute("SELECT 1")
        return {"status": "healthy"}
    except Exception as e:
        raise HTTPException(status_code=503, detail="Service unavailable")


# 服务信息端点
@app.get("/info", response_model=Dict[str, str])
def service_info():
    return {
        "service_name": "alert-service",
        "version": "1.0.0",
        "status": "running",
        "environment": settings.ENVIRONMENT
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG
    )
