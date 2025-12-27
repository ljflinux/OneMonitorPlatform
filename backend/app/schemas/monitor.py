from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
from datetime import datetime


class MonitorObjectBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    type: str = Field(..., description="server, network, storage, cloud, k8s, business")
    ip_address: Optional[str] = Field(None, max_length=50)
    hostname: Optional[str] = Field(None, max_length=100)
    location: Optional[str] = Field(None, max_length=100)
    description: Optional[str] = Field(None, max_length=255)
    configuration: Optional[Dict[str, Any]] = None


class MonitorObjectCreate(MonitorObjectBase):
    pass


class MonitorObjectUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=100)
    status: Optional[str] = Field(None, description="active, inactive, maintenance")
    ip_address: Optional[str] = Field(None, max_length=50)
    hostname: Optional[str] = Field(None, max_length=100)
    location: Optional[str] = Field(None, max_length=100)
    description: Optional[str] = Field(None, max_length=255)
    configuration: Optional[Dict[str, Any]] = None


class MonitorObjectInDBBase(MonitorObjectBase):
    id: int
    status: str
    created_at: datetime
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True


class MonitorObject(MonitorObjectInDBBase):
    pass


class MonitorMetricBase(BaseModel):
    monitor_object_id: int
    name: str = Field(..., max_length=100)
    value: float
    unit: Optional[str] = Field(None, max_length=20)


class MonitorMetricCreate(MonitorMetricBase):
    pass


class MonitorMetric(MonitorMetricBase):
    id: int
    timestamp: datetime
    
    class Config:
        from_attributes = True


class MonitorMetricList(BaseModel):
    metrics: List[MonitorMetric]
    total: int