from sqlalchemy.orm import Session
from typing import List, Optional, Dict, Any
from datetime import datetime
from app.models.collector import (
    Collector, CollectorStatus, CollectionTask, 
    CollectedMetric, CollectedLog, CollectedTrace
)
from app.schemas.collector import (
    CollectorCreate, CollectorUpdate, CollectionTaskCreate,
    CollectionTaskUpdate, CollectedMetricCreate, CollectedLogCreate,
    CollectedTraceCreate
)


# Collector CRUD
def get_collector(db: Session, collector_id: int) -> Optional[Collector]:
    return db.query(Collector).filter(Collector.id == collector_id).first()


def get_collector_by_name(db: Session, name: str) -> Optional[Collector]:
    return db.query(Collector).filter(Collector.name == name).first()


def get_collectors(
    db: Session,
    collector_type: Optional[str] = None,
    status: Optional[CollectorStatus] = None,
    skip: int = 0,
    limit: int = 100
) -> List[Collector]:
    query = db.query(Collector)
    if collector_type:
        query = query.filter(Collector.collector_type == collector_type)
    if status:
        query = query.filter(Collector.status == status)
    return query.offset(skip).limit(limit).all()


def create_collector(db: Session, collector: CollectorCreate) -> Collector:
    db_collector = Collector(**collector.dict())
    db.add(db_collector)
    db.commit()
    db.refresh(db_collector)
    return db_collector


def update_collector(
    db: Session, collector_id: int, collector: CollectorUpdate
) -> Optional[Collector]:
    db_collector = get_collector(db, collector_id)
    if not db_collector:
        return None
    
    update_data = collector.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_collector, key, value)
    
    db.commit()
    db.refresh(db_collector)
    return db_collector


def delete_collector(db: Session, collector_id: int) -> Optional[Collector]:
    db_collector = get_collector(db, collector_id)
    if db_collector:
        db.delete(db_collector)
        db.commit()
    return db_collector


def update_collector_last_run(db: Session, collector_id: int) -> Optional[Collector]:
    db_collector = get_collector(db, collector_id)
    if db_collector:
        db_collector.last_run_at = datetime.utcnow()
        db.commit()
        db.refresh(db_collector)
    return db_collector


# Collection Task CRUD
def get_collection_task(db: Session, task_id: int) -> Optional[CollectionTask]:
    return db.query(CollectionTask).filter(CollectionTask.id == task_id).first()


def get_collection_task_by_task_id(db: Session, task_id: str) -> Optional[CollectionTask]:
    return db.query(CollectionTask).filter(CollectionTask.task_id == task_id).first()


def get_collection_tasks(
    db: Session,
    collector_id: Optional[int] = None,
    status: Optional[str] = None,
    skip: int = 0,
    limit: int = 100
) -> List[CollectionTask]:
    query = db.query(CollectionTask)
    if collector_id:
        query = query.filter(CollectionTask.collector_id == collector_id)
    if status:
        query = query.filter(CollectionTask.status == status)
    return query.order_by(CollectionTask.created_at.desc()).offset(skip).limit(limit).all()


def create_collection_task(db: Session, task: CollectionTaskCreate) -> CollectionTask:
    db_task = CollectionTask(**task.dict())
    db.add(db_task)
    db.commit()
    db.refresh(db_task)
    return db_task


def update_collection_task(
    db: Session, task_id: int, task: CollectionTaskUpdate
) -> Optional[CollectionTask]:
    db_task = get_collection_task(db, task_id)
    if not db_task:
        return None
    
    update_data = task.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_task, key, value)
    
    db.commit()
    db.refresh(db_task)
    return db_task


def update_collection_task_by_task_id(
    db: Session, task_id: str, task: CollectionTaskUpdate
) -> Optional[CollectionTask]:
    db_task = get_collection_task_by_task_id(db, task_id)
    if not db_task:
        return None
    
    update_data = task.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_task, key, value)
    
    db.commit()
    db.refresh(db_task)
    return db_task


# Collected Metric CRUD
def get_collected_metric(db: Session, metric_id: int) -> Optional[CollectedMetric]:
    return db.query(CollectedMetric).filter(CollectedMetric.id == metric_id).first()


def get_collected_metrics(
    db: Session,
    collector_id: Optional[int] = None,
    ci_id: Optional[int] = None,
    metric_name: Optional[str] = None,
    start_time: Optional[datetime] = None,
    end_time: Optional[datetime] = None,
    skip: int = 0,
    limit: int = 100
) -> List[CollectedMetric]:
    query = db.query(CollectedMetric)
    if collector_id:
        query = query.filter(CollectedMetric.collector_id == collector_id)
    if ci_id:
        query = query.filter(CollectedMetric.ci_id == ci_id)
    if metric_name:
        query = query.filter(CollectedMetric.metric_name == metric_name)
    if start_time:
        query = query.filter(CollectedMetric.timestamp >= start_time)
    if end_time:
        query = query.filter(CollectedMetric.timestamp <= end_time)
    
    return query.order_by(CollectedMetric.timestamp.desc()).offset(skip).limit(limit).all()


def create_collected_metric(db: Session, metric: CollectedMetricCreate) -> CollectedMetric:
    db_metric = CollectedMetric(**metric.dict())
    db.add(db_metric)
    db.commit()
    db.refresh(db_metric)
    return db_metric


def bulk_create_collected_metrics(db: Session, metrics: List[CollectedMetricCreate]) -> None:
    db_metrics = [CollectedMetric(**metric.dict()) for metric in metrics]
    db.add_all(db_metrics)
    db.commit()


# Collected Log CRUD
def get_collected_log(db: Session, log_id: int) -> Optional[CollectedLog]:
    return db.query(CollectedLog).filter(CollectedLog.id == log_id).first()


def get_collected_logs(
    db: Session,
    collector_id: Optional[int] = None,
    ci_id: Optional[int] = None,
    log_level: Optional[str] = None,
    log_source: Optional[str] = None,
    start_time: Optional[datetime] = None,
    end_time: Optional[datetime] = None,
    skip: int = 0,
    limit: int = 100
) -> List[CollectedLog]:
    query = db.query(CollectedLog)
    if collector_id:
        query = query.filter(CollectedLog.collector_id == collector_id)
    if ci_id:
        query = query.filter(CollectedLog.ci_id == ci_id)
    if log_level:
        query = query.filter(CollectedLog.log_level == log_level)
    if log_source:
        query = query.filter(CollectedLog.log_source == log_source)
    if start_time:
        query = query.filter(CollectedLog.timestamp >= start_time)
    if end_time:
        query = query.filter(CollectedLog.timestamp <= end_time)
    
    return query.order_by(CollectedLog.timestamp.desc()).offset(skip).limit(limit).all()


def create_collected_log(db: Session, log: CollectedLogCreate) -> CollectedLog:
    db_log = CollectedLog(**log.dict())
    db.add(db_log)
    db.commit()
    db.refresh(db_log)
    return db_log


def bulk_create_collected_logs(db: Session, logs: List[CollectedLogCreate]) -> None:
    db_logs = [CollectedLog(**log.dict()) for log in logs]
    db.add_all(db_logs)
    db.commit()


# Collected Trace CRUD
def get_collected_trace(db: Session, trace_id: int) -> Optional[CollectedTrace]:
    return db.query(CollectedTrace).filter(CollectedTrace.id == trace_id).first()


def get_collected_trace_by_trace_id(db: Session, trace_id: str) -> Optional[CollectedTrace]:
    return db.query(CollectedTrace).filter(CollectedTrace.trace_id == trace_id).first()


def get_collected_traces(
    db: Session,
    ci_id: Optional[int] = None,
    service_name: Optional[str] = None,
    start_time: Optional[datetime] = None,
    end_time: Optional[datetime] = None,
    skip: int = 0,
    limit: int = 100
) -> List[CollectedTrace]:
    query = db.query(CollectedTrace)
    if ci_id:
        query = query.filter(CollectedTrace.ci_id == ci_id)
    if service_name:
        query = query.filter(CollectedTrace.service_name == service_name)
    if start_time:
        query = query.filter(CollectedTrace.start_time >= start_time)
    if end_time:
        query = query.filter(CollectedTrace.start_time <= end_time)
    
    return query.order_by(CollectedTrace.start_time.desc()).offset(skip).limit(limit).all()


def create_collected_trace(db: Session, trace: CollectedTraceCreate) -> CollectedTrace:
    db_trace = CollectedTrace(**trace.dict())
    db.add(db_trace)
    db.commit()
    db.refresh(db_trace)
    return db_trace


def bulk_create_collected_traces(db: Session, traces: List[CollectedTraceCreate]) -> None:
    db_traces = [CollectedTrace(**trace.dict()) for trace in traces]
    db.add_all(db_traces)
    db.commit()


# Helper functions
def count_collectors(
    db: Session,
    collector_type: Optional[str] = None,
    status: Optional[CollectorStatus] = None
) -> int:
    query = db.query(Collector)
    if collector_type:
        query = query.filter(Collector.collector_type == collector_type)
    if status:
        query = query.filter(Collector.status == status)
    return query.count()


def count_collection_tasks(
    db: Session,
    collector_id: Optional[int] = None,
    status: Optional[str] = None
) -> int:
    query = db.query(CollectionTask)
    if collector_id:
        query = query.filter(CollectionTask.collector_id == collector_id)
    if status:
        query = query.filter(CollectionTask.status == status)
    return query.count()


def count_collected_metrics(
    db: Session,
    collector_id: Optional[int] = None,
    ci_id: Optional[int] = None
) -> int:
    query = db.query(CollectedMetric)
    if collector_id:
        query = query.filter(CollectedMetric.collector_id == collector_id)
    if ci_id:
        query = query.filter(CollectedMetric.ci_id == ci_id)
    return query.count()


def count_collected_logs(
    db: Session,
    collector_id: Optional[int] = None,
    ci_id: Optional[int] = None
) -> int:
    query = db.query(CollectedLog)
    if collector_id:
        query = query.filter(CollectedLog.collector_id == collector_id)
    if ci_id:
        query = query.filter(CollectedLog.ci_id == ci_id)
    return query.count()