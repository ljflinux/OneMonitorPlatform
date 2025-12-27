from fastapi import APIRouter, Depends, HTTPException, status, Query, BackgroundTasks
from sqlalchemy.orm import Session
from typing import List, Optional, Dict, Any
from datetime import datetime
from app.db.session import get_db
from app.schemas import collector as collector_schemas
from app.crud import crud_collector
from app.models.collector import CollectorStatus, CollectorType
from app.core.config import settings
from app.utils.service_client import ServiceClient

router = APIRouter()

# Create service clients
cmdb_client = ServiceClient(settings.CMDB_SERVICE_URL)


# Collector Endpoints

@router.get("/collectors", response_model=collector_schemas.CollectorListResponse)
def get_collectors(
    collector_type: Optional[CollectorType] = Query(None, description="Collector type"),
    status: Optional[CollectorStatus] = Query(None, description="Collector status"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取采集器列表"""
    collectors = crud_collector.get_collectors(
        db, collector_type=collector_type, status=status, skip=skip, limit=limit
    )
    total = crud_collector.count_collectors(db, collector_type=collector_type, status=status)
    return collector_schemas.CollectorListResponse(total=total, items=collectors)


@router.get("/collectors/{collector_id}", response_model=collector_schemas.Collector)
def get_collector(
    collector_id: int,
    db: Session = Depends(get_db)
):
    """获取单个采集器"""
    db_collector = crud_collector.get_collector(db, collector_id)
    if not db_collector:
        raise HTTPException(status_code=404, detail="采集器不存在")
    return db_collector


@router.get("/collectors/{collector_id}/with-tasks", response_model=collector_schemas.CollectorWithTasks)
def get_collector_with_tasks(
    collector_id: int,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db)
):
    """获取采集器及其任务列表"""
    db_collector = crud_collector.get_collector(db, collector_id)
    if not db_collector:
        raise HTTPException(status_code=404, detail="采集器不存在")
    
    # Load tasks
    from app.schemas.collector import CollectionTask
    tasks = crud_collector.get_collection_tasks(db, collector_id=collector_id, skip=skip, limit=limit)
    
    collector_dict = db_collector.__dict__.copy()
    collector_dict.pop("_sa_instance_state", None)
    collector_dict["tasks"] = tasks
    
    return collector_schemas.CollectorWithTasks(**collector_dict)


@router.post("/collectors", response_model=collector_schemas.Collector, status_code=status.HTTP_201_CREATED)
def create_collector(
    collector: collector_schemas.CollectorCreate,
    db: Session = Depends(get_db)
):
    """创建采集器"""
    # Check if collector name already exists
    existing = crud_collector.get_collector_by_name(db, name=collector.name)
    if existing:
        raise HTTPException(status_code=400, detail="采集器名称已存在")
    
    # Validate CI exists if provided
    if collector.ci_id:
        try:
            ci_response = cmdb_client.get(f"/cmdb/cis/{collector.ci_id}")
            if ci_response.status_code != 200:
                raise HTTPException(status_code=404, detail="关联的CI不存在")
        except Exception as e:
            raise HTTPException(status_code=500, detail="无法连接到CMDB服务")
    
    return crud_collector.create_collector(db, collector)


@router.put("/collectors/{collector_id}", response_model=collector_schemas.Collector)
def update_collector(
    collector_id: int,
    collector: collector_schemas.CollectorUpdate,
    db: Session = Depends(get_db)
):
    """更新采集器"""
    # Validate CI exists if provided
    if collector.ci_id:
        try:
            ci_response = cmdb_client.get(f"/cmdb/cis/{collector.ci_id}")
            if ci_response.status_code != 200:
                raise HTTPException(status_code=404, detail="关联的CI不存在")
        except Exception as e:
            raise HTTPException(status_code=500, detail="无法连接到CMDB服务")
    
    db_collector = crud_collector.update_collector(db, collector_id, collector)
    if not db_collector:
        raise HTTPException(status_code=404, detail="采集器不存在")
    return db_collector


@router.delete("/collectors/{collector_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_collector(
    collector_id: int,
    db: Session = Depends(get_db)
):
    """删除采集器"""
    db_collector = crud_collector.delete_collector(db, collector_id)
    if not db_collector:
        raise HTTPException(status_code=404, detail="采集器不存在")
    return None


@router.patch("/collectors/{collector_id}/status", response_model=collector_schemas.Collector)
def update_collector_status(
    collector_id: int,
    status: CollectorStatus,
    db: Session = Depends(get_db)
):
    """更新采集器状态"""
    collector_update = collector_schemas.CollectorUpdate(status=status)
    db_collector = crud_collector.update_collector(db, collector_id, collector_update)
    if not db_collector:
        raise HTTPException(status_code=404, detail="采集器不存在")
    return db_collector


# Collection Task Endpoints

@router.get("/tasks", response_model=collector_schemas.CollectionTaskListResponse)
def get_collection_tasks(
    collector_id: Optional[int] = Query(None, description="采集器ID"),
    status: Optional[str] = Query(None, description="任务状态"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取采集任务列表"""
    tasks = crud_collector.get_collection_tasks(
        db, collector_id=collector_id, status=status, skip=skip, limit=limit
    )
    total = crud_collector.count_collection_tasks(db, collector_id=collector_id, status=status)
    return collector_schemas.CollectionTaskListResponse(total=total, items=tasks)


@router.get("/tasks/{task_id}", response_model=collector_schemas.CollectionTask)
def get_collection_task(
    task_id: int,
    db: Session = Depends(get_db)
):
    """获取单个采集任务"""
    db_task = crud_collector.get_collection_task(db, task_id)
    if not db_task:
        raise HTTPException(status_code=404, detail="采集任务不存在")
    return db_task


@router.get("/tasks/by-task-id/{task_id}", response_model=collector_schemas.CollectionTask)
def get_collection_task_by_task_id(
    task_id: str,
    db: Session = Depends(get_db)
):
    """通过Celery任务ID获取采集任务"""
    db_task = crud_collector.get_collection_task_by_task_id(db, task_id)
    if not db_task:
        raise HTTPException(status_code=404, detail="采集任务不存在")
    return db_task


# Collected Metrics Endpoints

@router.get("/metrics", response_model=collector_schemas.MetricsListResponse)
def get_collected_metrics(
    collector_id: Optional[int] = Query(None, description="采集器ID"),
    ci_id: Optional[int] = Query(None, description="CI ID"),
    metric_name: Optional[str] = Query(None, description="指标名称"),
    start_time: Optional[datetime] = Query(None, description="开始时间"),
    end_time: Optional[datetime] = Query(None, description="结束时间"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取采集的指标数据"""
    metrics = crud_collector.get_collected_metrics(
        db, collector_id=collector_id, ci_id=ci_id, metric_name=metric_name,
        start_time=start_time, end_time=end_time, skip=skip, limit=limit
    )
    total = crud_collector.count_collected_metrics(db, collector_id=collector_id, ci_id=ci_id)
    return collector_schemas.MetricsListResponse(total=total, items=metrics)


# Collected Logs Endpoints

@router.get("/logs", response_model=collector_schemas.LogsListResponse)
def get_collected_logs(
    collector_id: Optional[int] = Query(None, description="采集器ID"),
    ci_id: Optional[int] = Query(None, description="CI ID"),
    log_level: Optional[str] = Query(None, description="日志级别"),
    log_source: Optional[str] = Query(None, description="日志源"),
    start_time: Optional[datetime] = Query(None, description="开始时间"),
    end_time: Optional[datetime] = Query(None, description="结束时间"),
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取采集的日志数据"""
    logs = crud_collector.get_collected_logs(
        db, collector_id=collector_id, ci_id=ci_id, log_level=log_level,
        log_source=log_source, start_time=start_time, end_time=end_time, skip=skip, limit=limit
    )
    total = crud_collector.count_collected_logs(db, collector_id=collector_id, ci_id=ci_id)
    return collector_schemas.LogsListResponse(total=total, items=logs)


# Utility Endpoints

@router.post("/collectors/{collector_id}/run", response_model=Dict[str, Any])
def run_collector(
    collector_id: int,
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db)
):
    """手动触发采集器运行"""
    # Get collector
    db_collector = crud_collector.get_collector(db, collector_id)
    if not db_collector:
        raise HTTPException(status_code=404, detail="采集器不存在")
    
    if db_collector.status != CollectorStatus.ACTIVE:
        raise HTTPException(status_code=400, detail="采集器未处于活跃状态")
    
    # In a real implementation, this would send a task to Celery
    # For now, we'll just update the last run time
    updated_collector = crud_collector.update_collector_last_run(db, collector_id)
    
    # Simulate task creation
    from app.schemas.collector import CollectionTaskCreate
    task_data = CollectionTaskCreate(
        collector_id=collector_id,
        task_id=f"manual-{collector_id}-{datetime.utcnow().timestamp()}",
        status="running"
    )
    db_task = crud_collector.create_collection_task(db, task_data)
    
    # In a real implementation, we would add a background task to run the collector
    # background_tasks.add_task(run_collector_task, collector_id, db_task.id)
    
    return {
        "message": "采集器运行已触发",
        "collector_id": collector_id,
        "task_id": db_task.task_id
    }