import httpx
from typing import Dict, Any, Optional, List
from app.core.config import settings
import logging

logger = logging.getLogger(__name__)


class ServiceClient:
    """服务间通信客户端"""
    
    def __init__(self, base_url: str, timeout: int = 30):
        self.base_url = base_url.rstrip('/')
        self.timeout = timeout
        
    async def _request(
        self, 
        method: str, 
        endpoint: str, 
        data: Optional[Dict[str, Any]] = None,
        params: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> Dict[str, Any]:
        """通用请求方法"""
        url = f"{self.base_url}{endpoint}"
        
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            try:
                response = await client.request(
                    method=method,
                    url=url,
                    json=data,
                    params=params,
                    headers=headers
                )
                response.raise_for_status()
                return response.json()
            except httpx.HTTPStatusError as e:
                logger.error(f"HTTP error occurred: {e}")
                raise
            except httpx.RequestError as e:
                logger.error(f"Request error occurred: {e}")
                raise
            except Exception as e:
                logger.error(f"Unexpected error occurred: {e}")
                raise
    
    async def get(
        self, 
        endpoint: str, 
        params: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> Dict[str, Any]:
        """GET请求"""
        return await self._request("GET", endpoint, params=params, headers=headers)
    
    async def post(
        self, 
        endpoint: str, 
        data: Optional[Dict[str, Any]] = None,
        params: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> Dict[str, Any]:
        """POST请求"""
        return await self._request("POST", endpoint, data=data, params=params, headers=headers)
    
    async def put(
        self, 
        endpoint: str, 
        data: Optional[Dict[str, Any]] = None,
        params: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> Dict[str, Any]:
        """PUT请求"""
        return await self._request("PUT", endpoint, data=data, params=params, headers=headers)
    
    async def delete(
        self, 
        endpoint: str, 
        params: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> Dict[str, Any]:
        """DELETE请求"""
        return await self._request("DELETE", endpoint, params=params, headers=headers)


# 服务客户端实例
class ServiceClients:
    """所有服务客户端的集合"""
    
    def __init__(self):
        self.auth_service = ServiceClient("http://auth-service:8000")
        self.permission_service = ServiceClient("http://permission-service:8000")
        self.alert_service = ServiceClient("http://alert-service:8000")
        self.ticket_service = ServiceClient("http://ticket-service:8000")
        self.collector_service = ServiceClient("http://collector-service:8000")


# 创建全局服务客户端实例
service_clients = ServiceClients()