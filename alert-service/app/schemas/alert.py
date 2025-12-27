from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime
from enum import Enum


class AlertSeverity(str, Enum):
    INFO = "info"
    WARNING = "warning"
    ERROR = "error"
    CRITICAL = "critical"


class AlertStatus(str, Enum):
    FIRING = "firing"
    RESOLVED = "resolved"
    SILENCED = "silenced"
    ACKNOWLEDGED = "acknowledged"


class AlertRuleStatus(str, Enum):
    ACTIVE = "active"
    INACTIVE = "inactive"
    PAUSED = "paused"


class AlertRuleType(str, Enum):
    METRIC = "metric"
    LOG = "log"
    TRACE = "trace"
    CUSTOM = "custom"


class NotificationChannelType(str, Enum):
    EMAIL = "email"
    SMS = "sms"
    WECHAT = "wechat"
    DINGTALK = "dingtalk"
    SLACK = "slack"
    TEAMS = "teams"
    API = "api"
    SYSLOG = "syslog"


# Alert Rule schemas
class AlertRuleBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=200, description="告警规则名称")
    description: Optional[str] = Field(None, description="告警规则描述")
    rule_type: AlertRuleType = Field(..., description="告警规则类型")
    severity: AlertSeverity = Field(..., description="告警级别")
    condition: Dict[str, Any] = Field(..., description="告警条件配置")
    threshold: float = Field(..., description="告警阈值")
    comparison_operator: str = Field(..., description="比较运算符")
    duration: int = Field(..., description="持续时间（秒）")
    evaluation_interval: int = Field(default=60, description="评估间隔（秒）")
    tags: Optional[Dict[str, Any]] = Field(None, description="告警标签")
    ci_id: Optional[int] = Field(None, description="关联的CI ID")


class AlertRuleCreate(AlertRuleBase):
    pass


class AlertRuleUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=200, description="告警规则名称")
    description: Optional[str] = Field(None, description="告警规则描述")
    status: Optional[AlertRuleStatus] = Field(None, description="告警规则状态")
    severity: Optional[AlertSeverity] = Field(None, description="告警级别")
    condition: Optional[Dict[str, Any]] = Field(None, description="告警条件配置")
    threshold: Optional[float] = Field(None, description="告警阈值")
    comparison_operator: Optional[str] = Field(None, description="比较运算符")
    duration: Optional[int] = Field(None, description="持续时间（秒）")
    evaluation_interval: Optional[int] = Field(None, description="评估间隔（秒）")
    tags: Optional[Dict[str, Any]] = Field(None, description="告警标签")
    ci_id: Optional[int] = Field(None, description="关联的CI ID")


class AlertRule(AlertRuleBase):
    id: int
    status: AlertRuleStatus
    created_by: Optional[str]
    created_at: datetime
    updated_at: Optional[datetime]
    
    class Config:
        from_attributes = True


# Alert schemas
class AlertBase(BaseModel):
    alert_rule_id: int = Field(..., description="告警规则ID")
    title: str = Field(..., min_length=1, max_length=200, description="告警标题")
    message: str = Field(..., description="告警消息")
    source: str = Field(..., description="告警来源")
    source_id: Optional[str] = Field(None, description="来源ID")
    labels: Optional[Dict[str, Any]] = Field(None, description="告警标签")
    annotations: Optional[Dict[str, Any]] = Field(None, description="告警注释")
    ci_id: Optional[int] = Field(None, description="关联的CI ID")


class AlertCreate(AlertBase):
    severity: AlertSeverity = Field(..., description="告警级别")


class AlertUpdate(BaseModel):
    status: Optional[AlertStatus] = Field(None, description="告警状态")
    acknowledged_by: Optional[str] = Field(None, description="确认人")
    resolved_at: Optional[datetime] = Field(None, description="解决时间")
    silenced_until: Optional[datetime] = Field(None, description="静默结束时间")


class Alert(AlertBase):
    id: int
    status: AlertStatus
    severity: AlertSeverity
    firing_at: datetime
    resolved_at: Optional[datetime]
    acknowledged_at: Optional[datetime]
    acknowledged_by: Optional[str]
    silenced_until: Optional[datetime]
    created_at: datetime
    updated_at: Optional[datetime]
    
    class Config:
        from_attributes = True


class AlertWithRule(Alert):
    rule: AlertRule = Field(..., description="关联的告警规则")
    
    class Config:
        from_attributes = True


# Alert Group schemas
class AlertGroupBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=200, description="告警分组名称")
    description: Optional[str] = Field(None, description="告警分组描述")
    is_default: Optional[bool] = Field(False, description="是否为默认分组")


class AlertGroupCreate(AlertGroupBase):
    pass


class AlertGroupUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=200, description="告警分组名称")
    description: Optional[str] = Field(None, description="告警分组描述")
    is_default: Optional[bool] = Field(None, description="是否为默认分组")


class AlertGroup(AlertGroupBase):
    id: int
    created_by: Optional[str]
    created_at: datetime
    updated_at: Optional[datetime]
    
    class Config:
        from_attributes = True


class AlertGroupWithRules(AlertGroup):
    alert_rules: List[AlertRule] = Field([], description="关联的告警规则")
    
    class Config:
        from_attributes = True


# Notification Channel schemas
class NotificationChannelBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=200, description="通知渠道名称")
    channel_type: NotificationChannelType = Field(..., description="通知渠道类型")
    config: Dict[str, Any] = Field(..., description="通知渠道配置")
    is_enabled: Optional[bool] = Field(True, description="是否启用")


class NotificationChannelCreate(NotificationChannelBase):
    pass


class NotificationChannelUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=200, description="通知渠道名称")
    channel_type: Optional[NotificationChannelType] = Field(None, description="通知渠道类型")
    config: Optional[Dict[str, Any]] = Field(None, description="通知渠道配置")
    is_enabled: Optional[bool] = Field(None, description="是否启用")


class NotificationChannel(NotificationChannelBase):
    id: int
    created_by: Optional[str]
    created_at: datetime
    updated_at: Optional[datetime]
    
    class Config:
        from_attributes = True


# Alert Action schemas
class AlertActionBase(BaseModel):
    alert_id: int = Field(..., description="告警ID")
    action_type: str = Field(..., description="动作类型")
    status: str = Field(..., description="动作状态")
    action_result: Optional[Dict[str, Any]] = Field(None, description="动作结果")
    executed_by: Optional[str] = Field(None, description="执行人员")


class AlertActionCreate(AlertActionBase):
    pass


class AlertActionUpdate(BaseModel):
    status: Optional[str] = Field(None, description="动作状态")
    action_result: Optional[Dict[str, Any]] = Field(None, description="动作结果")
    executed_by: Optional[str] = Field(None, description="执行人员")


class AlertAction(AlertActionBase):
    id: int
    executed_at: datetime
    
    class Config:
        from_attributes = True


# Alert Silence schemas
class AlertSilenceBase(BaseModel):
    alert_id: Optional[int] = Field(None, description="告警ID")
    alert_rule_id: Optional[int] = Field(None, description="告警规则ID")
    silence_reason: str = Field(..., description="静默原因")
    ends_at: datetime = Field(..., description="静默结束时间")


class AlertSilenceCreate(AlertSilenceBase):
    pass


class AlertSilence(AlertSilenceBase):
    id: int
    is_active: bool
    created_by: Optional[str]
    created_at: datetime
    
    class Config:
        from_attributes = True


# Response schemas
class AlertRuleListResponse(BaseModel):
    total: int
    items: List[AlertRule]


class AlertListResponse(BaseModel):
    total: int
    items: List[Alert]


class AlertGroupListResponse(BaseModel):
    total: int
    items: List[AlertGroup]


class NotificationChannelListResponse(BaseModel):
    total: int
    items: List[NotificationChannel]


class AlertActionListResponse(BaseModel):
    total: int
    items: List[AlertAction]
