from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, JSON, Boolean, Enum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.db.session import Base
import enum


class CollectorType(enum.Enum):
    METRICS = "metrics"
    LOGS = "logs"
    TRACES = "traces"
    SNMP = "snmp"
    ICMP = "icmp"
    API = "api"
    CUSTOM = "custom"


class CollectorStatus(enum.Enum):
    ACTIVE = "active"
    INACTIVE = "inactive"
    ERROR = "error"
    PAUSED = "paused"


class Collector(Base):
    __tablename__ = "collectors"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False, index=True)
    collector_type = Column(Enum(CollectorType), nullable=False)
    status = Column(Enum(CollectorStatus), default=CollectorStatus.ACTIVE, nullable=False)
    description = Column(Text, nullable=True)
    config = Column(JSON, nullable=False)
    schedule = Column(String(100), nullable=True)  # Cron expression for scheduling
    ci_id = Column(Integer, ForeignKey("cis.id"), nullable=True)  # Reference to CMDB CI
    created_by = Column(String(100), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    last_run_at = Column(DateTime(timezone=True), nullable=True)
    next_run_at = Column(DateTime(timezone=True), nullable=True)
    
    # Relationships
    tasks = relationship("CollectionTask", back_populates="collector", cascade="all, delete-orphan")
    metrics = relationship("CollectedMetric", back_populates="collector")
    logs = relationship("CollectedLog", back_populates="collector")


class CollectionTask(Base):
    __tablename__ = "collection_tasks"
    
    id = Column(Integer, primary_key=True, index=True)
    collector_id = Column(Integer, ForeignKey("collectors.id"), nullable=False)
    task_id = Column(String(200), nullable=False, index=True)  # Celery task ID
    status = Column(String(50), nullable=False)  # pending, running, success, failure
    started_at = Column(DateTime(timezone=True), nullable=True)
    completed_at = Column(DateTime(timezone=True), nullable=True)
    result = Column(JSON, nullable=True)
    error = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    collector = relationship("Collector", back_populates="tasks")


class CollectedMetric(Base):
    __tablename__ = "collected_metrics"
    
    id = Column(Integer, primary_key=True, index=True)
    collector_id = Column(Integer, ForeignKey("collectors.id"), nullable=False)
    ci_id = Column(Integer, ForeignKey("cis.id"), nullable=True)
    metric_name = Column(String(200), nullable=False, index=True)
    metric_value = Column(String(100), nullable=False)
    metric_type = Column(String(50), nullable=False)  # gauge, counter, histogram, summary
    labels = Column(JSON, nullable=True)
    timestamp = Column(DateTime(timezone=True), server_default=func.now(), index=True)
    
    # Relationships
    collector = relationship("Collector", back_populates="metrics")


class CollectedLog(Base):
    __tablename__ = "collected_logs"
    
    id = Column(Integer, primary_key=True, index=True)
    collector_id = Column(Integer, ForeignKey("collectors.id"), nullable=False)
    ci_id = Column(Integer, ForeignKey("cis.id"), nullable=True)
    log_level = Column(String(50), nullable=False, index=True)
    log_message = Column(Text, nullable=False)
    log_source = Column(String(200), nullable=True, index=True)
    log_attributes = Column(JSON, nullable=True)
    timestamp = Column(DateTime(timezone=True), server_default=func.now(), index=True)
    
    # Relationships
    collector = relationship("Collector", back_populates="logs")


class CollectedTrace(Base):
    __tablename__ = "collected_traces"
    
    id = Column(Integer, primary_key=True, index=True)
    trace_id = Column(String(100), nullable=False, index=True)
    span_id = Column(String(100), nullable=False, index=True)
    parent_span_id = Column(String(100), nullable=True, index=True)
    ci_id = Column(Integer, ForeignKey("cis.id"), nullable=True)
    service_name = Column(String(200), nullable=False, index=True)
    operation_name = Column(String(200), nullable=False)
    start_time = Column(DateTime(timezone=True), nullable=False, index=True)
    end_time = Column(DateTime(timezone=True), nullable=False)
    duration_ms = Column(Integer, nullable=False)
    status_code = Column(Integer, nullable=True)
    status_message = Column(Text, nullable=True)
    attributes = Column(JSON, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
