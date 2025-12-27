from fastapi import Request, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from app.utils.service_client import service_clients
import logging

logger = logging.getLogger(__name__)
security = HTTPBearer()


async def get_current_user(credentials: HTTPAuthorizationCredentials = security):
    """获取当前用户信息"""
    token = credentials.credentials
    try:
        # 调用认证服务验证令牌
        user_info = await service_clients.auth_service.get(
            "/auth/verify",
            headers={"Authorization": f"Bearer {token}"}
        )
        return user_info
    except Exception as e:
        logger.error(f"Token verification failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )


async def check_permission(user: dict, resource: str, action: str):
    """检查用户权限"""
    try:
        # 调用权限服务检查权限
        permission_result = await service_clients.permission_service.post(
            "/permissions/check",
            data={
                "user_id": user["id"],
                "resource": resource,
                "action": action
            }
        )
        
        if not permission_result.get("has_permission", False):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Permission denied"
            )
        
        return permission_result
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Permission check failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Permission check failed"
        )


async def verify_and_check_permission(
    credentials: HTTPAuthorizationCredentials = security,
    resource: str = None,
    action: str = None
):
    """验证令牌并检查权限"""
    user = await get_current_user(credentials)
    if resource and action:
        await check_permission(user, resource, action)
    return user