from fastapi import APIRouter, Depends, HTTPException, Query, Path
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime

from app.db.session import get_db
from app.crud import crud_alert
from app.schemas.alert import (
    AlertRule, AlertRuleCreate, AlertRuleUpdate, AlertRuleListResponse,
    Alert, AlertCreate, AlertUpdate, AlertListResponse, AlertWithRule,
    AlertGroup, AlertGroupCreate, AlertGroupUpdate, AlertGroupListResponse,
    AlertGroupWithRules,
    NotificationChannel, NotificationChannelCreate, NotificationChannelUpdate,
    NotificationChannelListResponse,
    AlertAction, AlertActionCreate, AlertActionUpdate, AlertActionListResponse,
    AlertSilence, AlertSilenceCreate, AlertRuleStatus, AlertRuleType, AlertSeverity,
    AlertStatus, NotificationChannelType
)

router = APIRouter()


# Alert Rule Endpoints
@router.post("/rules", response_model=AlertRule, status_code=201)
def create_alert_rule(alert_rule: AlertRuleCreate, db: Session = Depends(get_db)):
    db_alert_rule = crud_alert.get_alert_rule_by_name(db, name=alert_rule.name)
    if db_alert_rule:
        raise HTTPException(status_code=400, detail="告警规则名称已存在")
    return crud_alert.create_alert_rule(db=db, alert_rule=alert_rule)


@router.get("/rules", response_model=AlertRuleListResponse)
def read_alert_rules(
    rule_type: Optional[AlertRuleType] = None,
    status: Optional[AlertRuleStatus] = None,
    severity: Optional[AlertSeverity] = None,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    alert_rules = crud_alert.get_alert_rules(
        db, rule_type=rule_type, status=status, severity=severity, skip=skip, limit=limit
    )
    total = crud_alert.count_alert_rules(
        db, rule_type=rule_type, status=status, severity=severity
    )
    return AlertRuleListResponse(total=total, items=alert_rules)


@router.get("/rules/{alert_rule_id}", response_model=AlertRule)
def read_alert_rule(alert_rule_id: int = Path(..., gt=0), db: Session = Depends(get_db)):
    db_alert_rule = crud_alert.get_alert_rule(db, alert_rule_id=alert_rule_id)
    if db_alert_rule is None:
        raise HTTPException(status_code=404, detail="告警规则不存在")
    return db_alert_rule


@router.put("/rules/{alert_rule_id}", response_model=AlertRule)
def update_alert_rule(
    alert_rule_id: int = Path(..., gt=0),
    alert_rule: AlertRuleUpdate = ...,
    db: Session = Depends(get_db)
):
    db_alert_rule = crud_alert.update_alert_rule(
        db, alert_rule_id=alert_rule_id, alert_rule=alert_rule
    )
    if db_alert_rule is None:
        raise HTTPException(status_code=404, detail="告警规则不存在")
    return db_alert_rule


@router.delete("/rules/{alert_rule_id}", response_model=AlertRule)
def delete_alert_rule(alert_rule_id: int = Path(..., gt=0), db: Session = Depends(get_db)):
    db_alert_rule = crud_alert.delete_alert_rule(db, alert_rule_id=alert_rule_id)
    if db_alert_rule is None:
        raise HTTPException(status_code=404, detail="告警规则不存在")
    return db_alert_rule


# Alert Endpoints
@router.post("/alerts", response_model=Alert, status_code=201)
def create_alert(alert: AlertCreate, db: Session = Depends(get_db)):
    return crud_alert.create_alert(db=db, alert=alert)


@router.get("/alerts", response_model=AlertListResponse)
def read_alerts(
    status: Optional[AlertStatus] = None,
    severity: Optional[AlertSeverity] = None,
    alert_rule_id: Optional[int] = None,
    ci_id: Optional[int] = None,
    start_time: Optional[datetime] = None,
    end_time: Optional[datetime] = None,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    alerts = crud_alert.get_alerts(
        db, status=status, severity=severity, alert_rule_id=alert_rule_id, 
        ci_id=ci_id, start_time=start_time, end_time=end_time, skip=skip, limit=limit
    )
    total = crud_alert.count_alerts(
        db, status=status, severity=severity, alert_rule_id=alert_rule_id, ci_id=ci_id
    )
    return AlertListResponse(total=total, items=alerts)


@router.get("/alerts/{alert_id}", response_model=Alert)
def read_alert(alert_id: int = Path(..., gt=0), db: Session = Depends(get_db)):
    db_alert = crud_alert.get_alert(db, alert_id=alert_id)
    if db_alert is None:
        raise HTTPException(status_code=404, detail="告警不存在")
    return db_alert


@router.put("/alerts/{alert_id}", response_model=Alert)
def update_alert(
    alert_id: int = Path(..., gt=0),
    alert: AlertUpdate = ...,
    db: Session = Depends(get_db)
):
    db_alert = crud_alert.update_alert(
        db, alert_id=alert_id, alert=alert
    )
    if db_alert is None:
        raise HTTPException(status_code=404, detail="告警不存在")
    return db_alert


@router.put("/alerts/{alert_id}/resolve", response_model=Alert)
def resolve_alert(
    alert_id: int = Path(..., gt=0),
    resolved_by: Optional[str] = None,
    db: Session = Depends(get_db)
):
    db_alert = crud_alert.resolve_alert(
        db, alert_id=alert_id, resolved_by=resolved_by
    )
    if db_alert is None:
        raise HTTPException(status_code=404, detail="告警不存在")
    return db_alert


# Alert Group Endpoints
@router.post("/groups", response_model=AlertGroup, status_code=201)
def create_alert_group(alert_group: AlertGroupCreate, db: Session = Depends(get_db)):
    db_alert_group = crud_alert.get_alert_group_by_name(db, name=alert_group.name)
    if db_alert_group:
        raise HTTPException(status_code=400, detail="告警分组名称已存在")
    return crud_alert.create_alert_group(db=db, alert_group=alert_group)


@router.get("/groups", response_model=AlertGroupListResponse)
def read_alert_groups(
    is_default: Optional[bool] = None,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    alert_groups = crud_alert.get_alert_groups(
        db, is_default=is_default, skip=skip, limit=limit
    )
    total = crud_alert.count_alert_groups(db, is_default=is_default)
    return AlertGroupListResponse(total=total, items=alert_groups)


@router.get("/groups/{alert_group_id}", response_model=AlertGroupWithRules)
def read_alert_group(alert_group_id: int = Path(..., gt=0), db: Session = Depends(get_db)):
    db_alert_group = crud_alert.get_alert_group(db, alert_group_id=alert_group_id)
    if db_alert_group is None:
        raise HTTPException(status_code=404, detail="告警分组不存在")
    return db_alert_group


@router.put("/groups/{alert_group_id}", response_model=AlertGroup)
def update_alert_group(
    alert_group_id: int = Path(..., gt=0),
    alert_group: AlertGroupUpdate = ...,
    db: Session = Depends(get_db)
):
    db_alert_group = crud_alert.update_alert_group(
        db, alert_group_id=alert_group_id, alert_group=alert_group
    )
    if db_alert_group is None:
        raise HTTPException(status_code=404, detail="告警分组不存在")
    return db_alert_group


@router.delete("/groups/{alert_group_id}", response_model=AlertGroup)
def delete_alert_group(alert_group_id: int = Path(..., gt=0), db: Session = Depends(get_db)):
    db_alert_group = crud_alert.delete_alert_group(db, alert_group_id=alert_group_id)
    if db_alert_group is None:
        raise HTTPException(status_code=404, detail="告警分组不存在")
    return db_alert_group


# Notification Channel Endpoints
@router.post("/channels", response_model=NotificationChannel, status_code=201)
def create_notification_channel(
    channel: NotificationChannelCreate,
    db: Session = Depends(get_db)
):
    db_channel = crud_alert.get_notification_channel_by_name(db, name=channel.name)
    if db_channel:
        raise HTTPException(status_code=400, detail="通知渠道名称已存在")
    return crud_alert.create_notification_channel(db=db, notification_channel=channel)


@router.get("/channels", response_model=NotificationChannelListResponse)
def read_notification_channels(
    channel_type: Optional[NotificationChannelType] = None,
    is_enabled: Optional[bool] = None,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    channels = crud_alert.get_notification_channels(
        db, channel_type=channel_type, is_enabled=is_enabled, skip=skip, limit=limit
    )
    total = crud_alert.count_notification_channels(
        db, channel_type=channel_type, is_enabled=is_enabled
    )
    return NotificationChannelListResponse(total=total, items=channels)


@router.get("/channels/{channel_id}", response_model=NotificationChannel)
def read_notification_channel(
    channel_id: int = Path(..., gt=0),
    db: Session = Depends(get_db)
):
    db_channel = crud_alert.get_notification_channel(db, notification_channel_id=channel_id)
    if db_channel is None:
        raise HTTPException(status_code=404, detail="通知渠道不存在")
    return db_channel


@router.put("/channels/{channel_id}", response_model=NotificationChannel)
def update_notification_channel(
    channel_id: int = Path(..., gt=0),
    channel: NotificationChannelUpdate = ...,
    db: Session = Depends(get_db)
):
    db_channel = crud_alert.update_notification_channel(
        db, channel_id=channel_id, notification_channel=channel
    )
    if db_channel is None:
        raise HTTPException(status_code=404, detail="通知渠道不存在")
    return db_channel


@router.delete("/channels/{channel_id}", response_model=NotificationChannel)
def delete_notification_channel(
    channel_id: int = Path(..., gt=0),
    db: Session = Depends(get_db)
):
    db_channel = crud_alert.delete_notification_channel(db, channel_id=channel_id)
    if db_channel is None:
        raise HTTPException(status_code=404, detail="通知渠道不存在")
    return db_channel


# Alert Action Endpoints
@router.post("/actions", response_model=AlertAction, status_code=201)
def create_alert_action(
    action: AlertActionCreate,
    db: Session = Depends(get_db)
):
    return crud_alert.create_alert_action(db=db, alert_action=action)


@router.get("/actions", response_model=AlertActionListResponse)
def read_alert_actions(
    alert_id: Optional[int] = None,
    action_type: Optional[str] = None,
    status: Optional[str] = None,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    actions = crud_alert.get_alert_actions(
        db, alert_id=alert_id, action_type=action_type, status=status, skip=skip, limit=limit
    )
    total = crud_alert.count_alert_actions(
        db, alert_id=alert_id, action_type=action_type, status=status
    )
    return AlertActionListResponse(total=total, items=actions)


@router.get("/actions/{action_id}", response_model=AlertAction)
def read_alert_action(
    action_id: int = Path(..., gt=0),
    db: Session = Depends(get_db)
):
    db_action = crud_alert.get_alert_action(db, action_id=action_id)
    if db_action is None:
        raise HTTPException(status_code=404, detail="告警动作不存在")
    return db_action


@router.put("/actions/{action_id}", response_model=AlertAction)
def update_alert_action(
    action_id: int = Path(..., gt=0),
    action: AlertActionUpdate = ...,
    db: Session = Depends(get_db)
):
    db_action = crud_alert.update_alert_action(
        db, action_id=action_id, alert_action=action
    )
    if db_action is None:
        raise HTTPException(status_code=404, detail="告警动作不存在")
    return db_action


# Alert Silence Endpoints
@router.post("/silences", response_model=AlertSilence, status_code=201)
def create_alert_silence(
    silence: AlertSilenceCreate,
    db: Session = Depends(get_db)
):
    return crud_alert.create_alert_silence(db=db, alert_silence=silence)


@router.get("/silences", response_model=List[AlertSilence])
def read_active_alert_silences(
    alert_id: Optional[int] = None,
    alert_rule_id: Optional[int] = None,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    return crud_alert.get_active_alert_silences(
        db, alert_id=alert_id, alert_rule_id=alert_rule_id, skip=skip, limit=limit
    )


@router.get("/silences/{silence_id}", response_model=AlertSilence)
def read_alert_silence(
    silence_id: int = Path(..., gt=0),
    db: Session = Depends(get_db)
):
    db_silence = crud_alert.get_alert_silence(db, silence_id=silence_id)
    if db_silence is None:
        raise HTTPException(status_code=404, detail="告警静默不存在")
    return db_silence


@router.put("/silences/{silence_id}/deactivate", response_model=AlertSilence)
def deactivate_alert_silence(
    silence_id: int = Path(..., gt=0),
    db: Session = Depends(get_db)
):
    db_silence = crud_alert.deactivate_alert_silence(db, silence_id=silence_id)
    if db_silence is None:
        raise HTTPException(status_code=404, detail="告警静默不存在")
    return db_silence
