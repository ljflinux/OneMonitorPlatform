from fastapi import APIRouter
from app.api.v1.endpoints import users, auth, monitor, alert, dashboard, cmdb

api_router = APIRouter()

# 认证相关路由
api_router.include_router(auth.router, prefix="/auth", tags=["认证"])

# 用户管理路由
api_router.include_router(users.router, prefix="/users", tags=["用户管理"])

# 监控管理路由
api_router.include_router(monitor.router, prefix="/monitor", tags=["监控管理"])

# 告警管理路由
api_router.include_router(alert.router, prefix="/alert", tags=["告警管理"])

# 配置管理路由
api_router.include_router(cmdb.router, prefix="/cmdb", tags=["配置管理"])

# 仪表盘路由
api_router.include_router(dashboard.router, prefix="/dashboard", tags=["仪表盘"])