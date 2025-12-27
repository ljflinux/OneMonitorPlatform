import httpx
from typing import Optional, Dict, Any
from app.core.config import settings


class ServiceClient:
    """服务客户端，用于微服务间通信"""
    
    def __init__(self, base_url: str):
        """初始化服务客户端
        
        Args:
            base_url: 服务基础URL
        """
        self.base_url = base_url
        self.client = httpx.AsyncClient()
    
    async def get(self, endpoint: str, params: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> httpx.Response:
        """发送GET请求
        
        Args:
            endpoint: API端点路径
            params: 查询参数
            headers: 请求头
            
        Returns:
            Response对象
        """
        url = f"{self.base_url}{endpoint}"
        return await self.client.get(url, params=params, headers=headers)
    
    async def post(self, endpoint: str, json: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> httpx.Response:
        """发送POST请求
        
        Args:
            endpoint: API端点路径
            json: 请求体JSON数据
            headers: 请求头
            
        Returns:
            Response对象
        """
        url = f"{self.base_url}{endpoint}"
        return await self.client.post(url, json=json, headers=headers)
    
    async def put(self, endpoint: str, json: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> httpx.Response:
        """发送PUT请求
        
        Args:
            endpoint: API端点路径
            json: 请求体JSON数据
            headers: 请求头
            
        Returns:
            Response对象
        """
        url = f"{self.base_url}{endpoint}"
        return await self.client.put(url, json=json, headers=headers)
    
    async def patch(self, endpoint: str, json: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> httpx.Response:
        """发送PATCH请求
        
        Args:
            endpoint: API端点路径
            json: 请求体JSON数据
            headers: 请求头
            
        Returns:
            Response对象
        """
        url = f"{self.base_url}{endpoint}"
        return await self.client.patch(url, json=json, headers=headers)
    
    async def delete(self, endpoint: str, headers: Optional[Dict[str, str]] = None) -> httpx.Response:
        """发送DELETE请求
        
        Args:
            endpoint: API端点路径
            headers: 请求头
            
        Returns:
            Response对象
        """
        url = f"{self.base_url}{endpoint}"
        return await self.client.delete(url, headers=headers)
    
    async def close(self):
        """关闭HTTP客户端"""
        await self.client.aclose()
    
    async def __aenter__(self):
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        await self.close()


# 创建服务客户端实例
cmdb_service_client = ServiceClient(settings.CMDB_SERVICE_URL)
