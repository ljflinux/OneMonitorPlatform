from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey, JSON
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.db.session import Base


class AlertRule(Base):
    __tablename__ = "alert_rules"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    description = Column(String(255), nullable=True)
    monitor_object_type = Column(String(50), nullable=False)
    metric_name = Column(String(100), nullable=False)
    condition = Column(String(20), nullable=False)  # >, <, >=, <=, ==, !=
    threshold = Column(String(50), nullable=False)
    severity = Column(String(20), nullable=False)  # info, warning, error, critical
    enabled = Column(Boolean, default=True)
    notification_channels = Column(JSON, nullable=True)  # email, sms, webhook, etc.
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    alerts = relationship("Alert", back_populates="alert_rule")


class Alert(Base):
    __tablename__ = "alerts"
    
    id = Column(Integer, primary_key=True, index=True)
    alert_rule_id = Column(Integer, ForeignKey("alert_rules.id"), nullable=False)
    monitor_object_id = Column(Integer, ForeignKey("monitor_objects.id"), nullable=False)
    severity = Column(String(20), nullable=False)
    message = Column(String(255), nullable=False)
    status = Column(String(20), default="firing")  # firing, resolved, acknowledged
    value = Column(String(50), nullable=False)
    acknowledged_by = Column(Integer, ForeignKey("users.id"), nullable=True)
    acknowledged_at = Column(DateTime(timezone=True), nullable=True)
    resolved_at = Column(DateTime(timezone=True), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    alert_rule = relationship("AlertRule", back_populates="alerts")
    monitor_object = relationship("MonitorObject", back_populates="alerts")