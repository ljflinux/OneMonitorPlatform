from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
from datetime import datetime


class AlertRuleBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    description: Optional[str] = Field(None, max_length=255)
    monitor_object_type: str = Field(..., max_length=50)
    metric_name: str = Field(..., max_length=100)
    condition: str = Field(..., description=">, <, >=, <=, ==, !=")
    threshold: str = Field(..., max_length=50)
    severity: str = Field(..., description="info, warning, error, critical")
    notification_channels: Optional[Dict[str, Any]] = None


class AlertRuleCreate(AlertRuleBase):
    pass


class AlertRuleUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=100)
    description: Optional[str] = Field(None, max_length=255)
    metric_name: Optional[str] = Field(None, max_length=100)
    condition: Optional[str] = None
    threshold: Optional[str] = Field(None, max_length=50)
    severity: Optional[str] = None
    enabled: Optional[bool] = None
    notification_channels: Optional[Dict[str, Any]] = None


class AlertRuleInDBBase(AlertRuleBase):
    id: int
    enabled: bool
    created_at: datetime
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True


class AlertRule(AlertRuleInDBBase):
    pass


class AlertBase(BaseModel):
    alert_rule_id: int
    monitor_object_id: int
    severity: str
    message: str = Field(..., max_length=255)
    value: str = Field(..., max_length=50)


class AlertCreate(AlertBase):
    pass


class AlertUpdate(BaseModel):
    status: Optional[str] = Field(None, description="firing, resolved, acknowledged")
    acknowledged_by: Optional[int] = None
    acknowledged_at: Optional[datetime] = None
    resolved_at: Optional[datetime] = None


class AlertInDBBase(AlertBase):
    id: int
    status: str
    acknowledged_by: Optional[int] = None
    acknowledged_at: Optional[datetime] = None
    resolved_at: Optional[datetime] = None
    created_at: datetime
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True


class Alert(AlertInDBBase):
    alert_rule: Optional[AlertRule] = None


class AlertList(BaseModel):
    alerts: List[Alert]
    total: int