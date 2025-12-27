from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime
from enum import Enum


class CollectorType(str, Enum):
    METRICS = "metrics"
    LOGS = "logs"
    TRACES = "traces"
    SNMP = "snmp"
    ICMP = "icmp"
    API = "api"
    CUSTOM = "custom"


class CollectorStatus(str, Enum):
    ACTIVE = "active"
    INACTIVE = "inactive"
    ERROR = "error"
    PAUSED = "paused"


# Collector schemas
class CollectorBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=200, description="Collector name")
    collector_type: CollectorType = Field(..., description="Type of collector")
    description: Optional[str] = Field(None, description="Collector description")
    config: Dict[str, Any] = Field(..., description="Collector configuration")
    schedule: Optional[str] = Field(None, description="Cron expression for scheduling")
    ci_id: Optional[int] = Field(None, description="CI ID from CMDB")


class CollectorCreate(CollectorBase):
    pass


class CollectorUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=200, description="Collector name")
    collector_type: Optional[CollectorType] = Field(None, description="Type of collector")
    status: Optional[CollectorStatus] = Field(None, description="Collector status")
    description: Optional[str] = Field(None, description="Collector description")
    config: Optional[Dict[str, Any]] = Field(None, description="Collector configuration")
    schedule: Optional[str] = Field(None, description="Cron expression for scheduling")
    ci_id: Optional[int] = Field(None, description="CI ID from CMDB")


class Collector(CollectorBase):
    id: int
    status: CollectorStatus
    created_by: Optional[str]
    created_at: datetime
    updated_at: Optional[datetime]
    last_run_at: Optional[datetime]
    next_run_at: Optional[datetime]
    
    class Config:
        from_attributes = True


class CollectorWithTasks(Collector):
    tasks: List["CollectionTask"] = []


# Collection Task schemas
class CollectionTaskBase(BaseModel):
    collector_id: int
    task_id: str
    status: str
    result: Optional[Dict[str, Any]] = None
    error: Optional[str] = None


class CollectionTaskCreate(CollectionTaskBase):
    pass


class CollectionTaskUpdate(BaseModel):
    status: Optional[str] = None
    started_at: Optional[datetime] = None
    completed_at: Optional[datetime] = None
    result: Optional[Dict[str, Any]] = None
    error: Optional[str] = None


class CollectionTask(CollectionTaskBase):
    id: int
    started_at: Optional[datetime]
    completed_at: Optional[datetime]
    created_at: datetime
    
    class Config:
        from_attributes = True


# Collected Metric schemas
class CollectedMetricBase(BaseModel):
    collector_id: int
    ci_id: Optional[int]
    metric_name: str
    metric_value: str
    metric_type: str
    labels: Optional[Dict[str, Any]] = None


class CollectedMetricCreate(CollectedMetricBase):
    pass


class CollectedMetric(CollectedMetricBase):
    id: int
    timestamp: datetime
    
    class Config:
        from_attributes = True


# Collected Log schemas
class CollectedLogBase(BaseModel):
    collector_id: int
    ci_id: Optional[int]
    log_level: str
    log_message: str
    log_source: Optional[str] = None
    log_attributes: Optional[Dict[str, Any]] = None


class CollectedLogCreate(CollectedLogBase):
    pass


class CollectedLog(CollectedLogBase):
    id: int
    timestamp: datetime
    
    class Config:
        from_attributes = True


# Collected Trace schemas
class CollectedTraceBase(BaseModel):
    trace_id: str
    span_id: str
    parent_span_id: Optional[str] = None
    ci_id: Optional[int]
    service_name: str
    operation_name: str
    start_time: datetime
    end_time: datetime
    duration_ms: int
    status_code: Optional[int] = None
    status_message: Optional[str] = None
    attributes: Optional[Dict[str, Any]] = None


class CollectedTraceCreate(CollectedTraceBase):
    pass


class CollectedTrace(CollectedTraceBase):
    id: int
    created_at: datetime
    
    class Config:
        from_attributes = True


# Response schemas
class CollectorListResponse(BaseModel):
    total: int
    items: List[Collector]


class CollectionTaskListResponse(BaseModel):
    total: int
    items: List[CollectionTask]


class MetricsListResponse(BaseModel):
    total: int
    items: List[CollectedMetric]


class LogsListResponse(BaseModel):
    total: int
    items: List[CollectedLog]


class TracesListResponse(BaseModel):
    total: int
    items: List[CollectedTrace]


# Link the recursive schema
CollectionTask.model_rebuild()