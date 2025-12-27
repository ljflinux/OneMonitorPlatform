from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, JSON, Boolean, Enum, Float
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.db.session import Base
import enum


class AlertSeverity(enum.Enum):
    INFO = "info"
    WARNING = "warning"
    ERROR = "error"
    CRITICAL = "critical"


class AlertStatus(enum.Enum):
    FIRING = "firing"
    RESOLVED = "resolved"
    SILENCED = "silenced"
    ACKNOWLEDGED = "acknowledged"


class AlertRuleStatus(enum.Enum):
    ACTIVE = "active"
    INACTIVE = "inactive"
    PAUSED = "paused"


class AlertRuleType(enum.Enum):
    METRIC = "metric"
    LOG = "log"
    TRACE = "trace"
    CUSTOM = "custom"


class NotificationChannelType(enum.Enum):
    EMAIL = "email"
    SMS = "sms"
    WECHAT = "wechat"
    DINGTALK = "dingtalk"
    SLACK = "slack"
    TEAMS = "teams"
    API = "api"
    SYSLOG = "syslog"


class AlertRule(Base):
    __tablename__ = "alert_rules"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, index=True)
    description = Column(Text, nullable=True)
    rule_type = Column(Enum(AlertRuleType), nullable=False)
    status = Column(Enum(AlertRuleStatus), default=AlertRuleStatus.ACTIVE, nullable=False)
    severity = Column(Enum(AlertSeverity), nullable=False)
    condition = Column(JSON, nullable=False)  # 告警条件配置
    threshold = Column(Float, nullable=False)
    comparison_operator = Column(String(10), nullable=False)  # >, <, >=, <=, ==, !=
    duration = Column(Integer, nullable=False)  # 持续时间（秒）
    evaluation_interval = Column(Integer, default=60)  # 评估间隔（秒）
    tags = Column(JSON, nullable=True)  # 告警标签
    ci_id = Column(Integer, ForeignKey("cis.id"), nullable=True)
    created_by = Column(String(100), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    alerts = relationship("Alert", back_populates="rule")
    alert_rule_channels = relationship("AlertRuleNotificationChannel", back_populates="alert_rule", cascade="all, delete-orphan")


class Alert(Base):
    __tablename__ = "alerts"
    
    id = Column(Integer, primary_key=True, index=True)
    alert_rule_id = Column(Integer, ForeignKey("alert_rules.id"), nullable=False)
    status = Column(Enum(AlertStatus), default=AlertStatus.FIRING, nullable=False, index=True)
    severity = Column(Enum(AlertSeverity), nullable=False, index=True)
    title = Column(String(200), nullable=False)
    message = Column(Text, nullable=False)
    source = Column(String(200), nullable=False)  # 告警来源
    source_id = Column(String(100), nullable=True)  # 来源ID（如Prometheus告警ID）
    labels = Column(JSON, nullable=True)
    annotations = Column(JSON, nullable=True)
    ci_id = Column(Integer, ForeignKey("cis.id"), nullable=True)
    firing_at = Column(DateTime(timezone=True), server_default=func.now(), index=True)
    resolved_at = Column(DateTime(timezone=True), nullable=True, index=True)
    acknowledged_at = Column(DateTime(timezone=True), nullable=True)
    acknowledged_by = Column(String(100), nullable=True)
    silenced_until = Column(DateTime(timezone=True), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    rule = relationship("AlertRule", back_populates="alerts")
    alert_actions = relationship("AlertAction", back_populates="alert", cascade="all, delete-orphan")


class AlertGroup(Base):
    __tablename__ = "alert_groups"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, unique=True)
    description = Column(Text, nullable=True)
    is_default = Column(Boolean, default=False)
    created_by = Column(String(100), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    alert_rules = relationship("AlertRule", secondary="alert_group_rules", backref="alert_groups")


class AlertGroupRule(Base):
    __tablename__ = "alert_group_rules"
    
    alert_group_id = Column(Integer, ForeignKey("alert_groups.id"), primary_key=True)
    alert_rule_id = Column(Integer, ForeignKey("alert_rules.id"), primary_key=True)


class AlertRuleNotificationChannel(Base):
    __tablename__ = "alert_rule_notification_channels"
    
    id = Column(Integer, primary_key=True, index=True)
    alert_rule_id = Column(Integer, ForeignKey("alert_rules.id"), nullable=False)
    channel_id = Column(Integer, ForeignKey("notification_channels.id"), nullable=False)
    is_enabled = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    alert_rule = relationship("AlertRule", back_populates="alert_rule_channels")
    channel = relationship("NotificationChannel", back_populates="alert_rule_channels")


class NotificationChannel(Base):
    __tablename__ = "notification_channels"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, unique=True)
    channel_type = Column(Enum(NotificationChannelType), nullable=False)
    config = Column(JSON, nullable=False)  # 通知渠道配置
    is_enabled = Column(Boolean, default=True)
    created_by = Column(String(100), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    alert_rule_channels = relationship("AlertRuleNotificationChannel", back_populates="channel")


class AlertAction(Base):
    __tablename__ = "alert_actions"
    
    id = Column(Integer, primary_key=True, index=True)
    alert_id = Column(Integer, ForeignKey("alerts.id"), nullable=False)
    action_type = Column(String(50), nullable=False)  # notification, ticket, auto_remediation
    status = Column(String(50), nullable=False)  # pending, success, failure
    action_result = Column(JSON, nullable=True)
    executed_at = Column(DateTime(timezone=True), server_default=func.now())
    executed_by = Column(String(100), nullable=True)
    
    # Relationships
    alert = relationship("Alert", back_populates="alert_actions")


class AlertSilence(Base):
    __tablename__ = "alert_silences"
    
    id = Column(Integer, primary_key=True, index=True)
    alert_id = Column(Integer, ForeignKey("alerts.id"), nullable=True)
    alert_rule_id = Column(Integer, ForeignKey("alert_rules.id"), nullable=True)
    silence_reason = Column(Text, nullable=False)
    started_at = Column(DateTime(timezone=True), server_default=func.now())
    ends_at = Column(DateTime(timezone=True), nullable=False)
    is_active = Column(Boolean, default=True)
    created_by = Column(String(100), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
