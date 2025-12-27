from sqlalchemy import Column, Integer, String, Boolean, DateTime, Float, ForeignKey, JSON
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.db.session import Base


class MonitorObject(Base):
    __tablename__ = "monitor_objects"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    type = Column(String(50), nullable=False)  # server, network, storage, cloud, k8s, business
    status = Column(String(20), default="active")  # active, inactive, maintenance
    ip_address = Column(String(50), nullable=True)
    hostname = Column(String(100), nullable=True)
    location = Column(String(100), nullable=True)
    description = Column(String(255), nullable=True)
    configuration = Column(JSON, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    metrics = relationship("MonitorMetric", back_populates="monitor_object")
    alerts = relationship("Alert", back_populates="monitor_object")


class MonitorMetric(Base):
    __tablename__ = "monitor_metrics"
    
    id = Column(Integer, primary_key=True, index=True)
    monitor_object_id = Column(Integer, ForeignKey("monitor_objects.id"), nullable=False)
    name = Column(String(100), nullable=False)
    value = Column(Float, nullable=False)
    unit = Column(String(20), nullable=True)
    timestamp = Column(DateTime(timezone=True), server_default=func.now())
    
    monitor_object = relationship("MonitorObject", back_populates="metrics")