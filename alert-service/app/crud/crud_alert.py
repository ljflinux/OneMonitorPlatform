from sqlalchemy.orm import Session
from typing import List, Optional, Dict, Any
from datetime import datetime
from app.models.alert import (
    AlertRule, AlertRuleStatus, AlertRuleType, AlertSeverity,
    Alert, AlertStatus, AlertGroup, AlertAction,
    NotificationChannel, NotificationChannelType, AlertSilence
)
from app.schemas.alert import (
    AlertRuleCreate, AlertRuleUpdate, AlertCreate, AlertUpdate,
    AlertGroupCreate, AlertGroupUpdate, AlertActionCreate,
    AlertActionUpdate, NotificationChannelCreate, NotificationChannelUpdate,
    AlertSilenceCreate
)


# Alert Rule CRUD
def get_alert_rule(db: Session, alert_rule_id: int) -> Optional[AlertRule]:
    return db.query(AlertRule).filter(AlertRule.id == alert_rule_id).first()


def get_alert_rule_by_name(db: Session, name: str) -> Optional[AlertRule]:
    return db.query(AlertRule).filter(AlertRule.name == name).first()


def get_alert_rules(
    db: Session,
    rule_type: Optional[AlertRuleType] = None,
    status: Optional[AlertRuleStatus] = None,
    severity: Optional[AlertSeverity] = None,
    skip: int = 0,
    limit: int = 100
) -> List[AlertRule]:
    query = db.query(AlertRule)
    if rule_type:
        query = query.filter(AlertRule.rule_type == rule_type)
    if status:
        query = query.filter(AlertRule.status == status)
    if severity:
        query = query.filter(AlertRule.severity == severity)
    return query.order_by(AlertRule.created_at.desc()).offset(skip).limit(limit).all()


def create_alert_rule(db: Session, alert_rule: AlertRuleCreate) -> AlertRule:
    db_alert_rule = AlertRule(**alert_rule.dict())
    db.add(db_alert_rule)
    db.commit()
    db.refresh(db_alert_rule)
    return db_alert_rule


def update_alert_rule(
    db: Session, alert_rule_id: int, alert_rule: AlertRuleUpdate
) -> Optional[AlertRule]:
    db_alert_rule = get_alert_rule(db, alert_rule_id)
    if not db_alert_rule:
        return None
    
    update_data = alert_rule.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_alert_rule, key, value)
    
    db.commit()
    db.refresh(db_alert_rule)
    return db_alert_rule


def delete_alert_rule(db: Session, alert_rule_id: int) -> Optional[AlertRule]:
    db_alert_rule = get_alert_rule(db, alert_rule_id)
    if db_alert_rule:
        db.delete(db_alert_rule)
        db.commit()
    return db_alert_rule


def count_alert_rules(
    db: Session,
    rule_type: Optional[AlertRuleType] = None,
    status: Optional[AlertRuleStatus] = None,
    severity: Optional[AlertSeverity] = None
) -> int:
    query = db.query(AlertRule)
    if rule_type:
        query = query.filter(AlertRule.rule_type == rule_type)
    if status:
        query = query.filter(AlertRule.status == status)
    if severity:
        query = query.filter(AlertRule.severity == severity)
    return query.count()


# Alert CRUD
def get_alert(db: Session, alert_id: int) -> Optional[Alert]:
    return db.query(Alert).filter(Alert.id == alert_id).first()


def get_alerts(
    db: Session,
    status: Optional[AlertStatus] = None,
    severity: Optional[AlertSeverity] = None,
    alert_rule_id: Optional[int] = None,
    ci_id: Optional[int] = None,
    start_time: Optional[datetime] = None,
    end_time: Optional[datetime] = None,
    skip: int = 0,
    limit: int = 100
) -> List[Alert]:
    query = db.query(Alert)
    if status:
        query = query.filter(Alert.status == status)
    if severity:
        query = query.filter(Alert.severity == severity)
    if alert_rule_id:
        query = query.filter(Alert.alert_rule_id == alert_rule_id)
    if ci_id:
        query = query.filter(Alert.ci_id == ci_id)
    if start_time:
        query = query.filter(Alert.firing_at >= start_time)
    if end_time:
        query = query.filter(Alert.firing_at <= end_time)
    return query.order_by(Alert.firing_at.desc()).offset(skip).limit(limit).all()


def create_alert(db: Session, alert: AlertCreate) -> Alert:
    db_alert = Alert(**alert.dict())
    db.add(db_alert)
    db.commit()
    db.refresh(db_alert)
    return db_alert


def update_alert(
    db: Session, alert_id: int, alert: AlertUpdate
) -> Optional[Alert]:
    db_alert = get_alert(db, alert_id)
    if not db_alert:
        return None
    
    update_data = alert.dict(exclude_unset=True)
    for key, value in update_data.items():
        if key == "status" and value == AlertStatus.ACKNOWLEDGED and "acknowledged_by" in update_data:
            update_data["acknowledged_at"] = datetime.utcnow()
        setattr(db_alert, key, value)
    
    db.commit()
    db.refresh(db_alert)
    return db_alert


def resolve_alert(
    db: Session, alert_id: int, resolved_by: Optional[str] = None
) -> Optional[Alert]:
    db_alert = get_alert(db, alert_id)
    if not db_alert:
        return None
    
    db_alert.status = AlertStatus.RESOLVED
    db_alert.resolved_at = datetime.utcnow()
    if resolved_by:
        db_alert.acknowledged_by = resolved_by
    
    db.commit()
    db.refresh(db_alert)
    return db_alert


def count_alerts(
    db: Session,
    status: Optional[AlertStatus] = None,
    severity: Optional[AlertSeverity] = None,
    alert_rule_id: Optional[int] = None,
    ci_id: Optional[int] = None
) -> int:
    query = db.query(Alert)
    if status:
        query = query.filter(Alert.status == status)
    if severity:
        query = query.filter(Alert.severity == severity)
    if alert_rule_id:
        query = query.filter(Alert.alert_rule_id == alert_rule_id)
    if ci_id:
        query = query.filter(Alert.ci_id == ci_id)
    return query.count()


# Alert Group CRUD
def get_alert_group(db: Session, alert_group_id: int) -> Optional[AlertGroup]:
    return db.query(AlertGroup).filter(AlertGroup.id == alert_group_id).first()


def get_alert_group_by_name(db: Session, name: str) -> Optional[AlertGroup]:
    return db.query(AlertGroup).filter(AlertGroup.name == name).first()


def get_alert_groups(
    db: Session,
    is_default: Optional[bool] = None,
    skip: int = 0,
    limit: int = 100
) -> List[AlertGroup]:
    query = db.query(AlertGroup)
    if is_default is not None:
        query = query.filter(AlertGroup.is_default == is_default)
    return query.order_by(AlertGroup.name).offset(skip).limit(limit).all()


def create_alert_group(db: Session, alert_group: AlertGroupCreate) -> AlertGroup:
    # 确保只有一个默认分组
    if alert_group.is_default:
        db.query(AlertGroup).filter(AlertGroup.is_default == True).update({"is_default": False})
    
    db_alert_group = AlertGroup(**alert_group.dict())
    db.add(db_alert_group)
    db.commit()
    db.refresh(db_alert_group)
    return db_alert_group


def update_alert_group(
    db: Session, alert_group_id: int, alert_group: AlertGroupUpdate
) -> Optional[AlertGroup]:
    db_alert_group = get_alert_group(db, alert_group_id)
    if not db_alert_group:
        return None
    
    update_data = alert_group.dict(exclude_unset=True)
    
    # 确保只有一个默认分组
    if "is_default" in update_data and update_data["is_default"]:
        db.query(AlertGroup).filter(AlertGroup.is_default == True).update({"is_default": False})
    
    for key, value in update_data.items():
        setattr(db_alert_group, key, value)
    
    db.commit()
    db.refresh(db_alert_group)
    return db_alert_group


def delete_alert_group(db: Session, alert_group_id: int) -> Optional[AlertGroup]:
    db_alert_group = get_alert_group(db, alert_group_id)
    if db_alert_group:
        db.delete(db_alert_group)
        db.commit()
    return db_alert_group


def count_alert_groups(
    db: Session,
    is_default: Optional[bool] = None
) -> int:
    query = db.query(AlertGroup)
    if is_default is not None:
        query = query.filter(AlertGroup.is_default == is_default)
    return query.count()


# Notification Channel CRUD
def get_notification_channel(
    db: Session, notification_channel_id: int
) -> Optional[NotificationChannel]:
    return db.query(NotificationChannel).filter(NotificationChannel.id == notification_channel_id).first()


def get_notification_channel_by_name(
    db: Session, name: str
) -> Optional[NotificationChannel]:
    return db.query(NotificationChannel).filter(NotificationChannel.name == name).first()


def get_notification_channels(
    db: Session,
    channel_type: Optional[NotificationChannelType] = None,
    is_enabled: Optional[bool] = None,
    skip: int = 0,
    limit: int = 100
) -> List[NotificationChannel]:
    query = db.query(NotificationChannel)
    if channel_type:
        query = query.filter(NotificationChannel.channel_type == channel_type)
    if is_enabled is not None:
        query = query.filter(NotificationChannel.is_enabled == is_enabled)
    return query.order_by(NotificationChannel.name).offset(skip).limit(limit).all()


def create_notification_channel(
    db: Session, notification_channel: NotificationChannelCreate
) -> NotificationChannel:
    db_channel = NotificationChannel(**notification_channel.dict())
    db.add(db_channel)
    db.commit()
    db.refresh(db_channel)
    return db_channel


def update_notification_channel(
    db: Session, channel_id: int, notification_channel: NotificationChannelUpdate
) -> Optional[NotificationChannel]:
    db_channel = get_notification_channel(db, channel_id)
    if not db_channel:
        return None
    
    update_data = notification_channel.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_channel, key, value)
    
    db.commit()
    db.refresh(db_channel)
    return db_channel


def delete_notification_channel(
    db: Session, channel_id: int
) -> Optional[NotificationChannel]:
    db_channel = get_notification_channel(db, channel_id)
    if db_channel:
        db.delete(db_channel)
        db.commit()
    return db_channel


def count_notification_channels(
    db: Session,
    channel_type: Optional[NotificationChannelType] = None,
    is_enabled: Optional[bool] = None
) -> int:
    query = db.query(NotificationChannel)
    if channel_type:
        query = query.filter(NotificationChannel.channel_type == channel_type)
    if is_enabled is not None:
        query = query.filter(NotificationChannel.is_enabled == is_enabled)
    return query.count()


# Alert Action CRUD
def get_alert_action(db: Session, action_id: int) -> Optional[AlertAction]:
    return db.query(AlertAction).filter(AlertAction.id == action_id).first()


def get_alert_actions(
    db: Session,
    alert_id: Optional[int] = None,
    action_type: Optional[str] = None,
    status: Optional[str] = None,
    skip: int = 0,
    limit: int = 100
) -> List[AlertAction]:
    query = db.query(AlertAction)
    if alert_id:
        query = query.filter(AlertAction.alert_id == alert_id)
    if action_type:
        query = query.filter(AlertAction.action_type == action_type)
    if status:
        query = query.filter(AlertAction.status == status)
    return query.order_by(AlertAction.executed_at.desc()).offset(skip).limit(limit).all()


def create_alert_action(
    db: Session, alert_action: AlertActionCreate
) -> AlertAction:
    db_action = AlertAction(**alert_action.dict())
    db.add(db_action)
    db.commit()
    db.refresh(db_action)
    return db_action


def update_alert_action(
    db: Session, action_id: int, alert_action: AlertActionUpdate
) -> Optional[AlertAction]:
    db_action = get_alert_action(db, action_id)
    if not db_action:
        return None
    
    update_data = alert_action.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_action, key, value)
    
    db.commit()
    db.refresh(db_action)
    return db_action


def count_alert_actions(
    db: Session,
    alert_id: Optional[int] = None,
    action_type: Optional[str] = None,
    status: Optional[str] = None
) -> int:
    query = db.query(AlertAction)
    if alert_id:
        query = query.filter(AlertAction.alert_id == alert_id)
    if action_type:
        query = query.filter(AlertAction.action_type == action_type)
    if status:
        query = query.filter(AlertAction.status == status)
    return query.count()


# Alert Silence CRUD
def get_alert_silence(db: Session, silence_id: int) -> Optional[AlertSilence]:
    return db.query(AlertSilence).filter(AlertSilence.id == silence_id).first()


def get_active_alert_silences(
    db: Session,
    alert_id: Optional[int] = None,
    alert_rule_id: Optional[int] = None,
    skip: int = 0,
    limit: int = 100
) -> List[AlertSilence]:
    query = db.query(AlertSilence).filter(
        AlertSilence.is_active == True,
        AlertSilence.ends_at > datetime.utcnow()
    )
    if alert_id:
        query = query.filter(AlertSilence.alert_id == alert_id)
    if alert_rule_id:
        query = query.filter(AlertSilence.alert_rule_id == alert_rule_id)
    return query.order_by(AlertSilence.ends_at.desc()).offset(skip).limit(limit).all()


def create_alert_silence(
    db: Session, alert_silence: AlertSilenceCreate
) -> AlertSilence:
    db_silence = AlertSilence(**alert_silence.dict())
    db.add(db_silence)
    db.commit()
    db.refresh(db_silence)
    return db_silence


def deactivate_alert_silence(
    db: Session, silence_id: int
) -> Optional[AlertSilence]:
    db_silence = get_alert_silence(db, silence_id)
    if not db_silence:
        return None
    
    db_silence.is_active = False
    db.commit()
    db.refresh(db_silence)
    return db_silence


def count_active_alert_silences(
    db: Session,
    alert_id: Optional[int] = None,
    alert_rule_id: Optional[int] = None
) -> int:
    query = db.query(AlertSilence).filter(
        AlertSilence.is_active == True,
        AlertSilence.ends_at > datetime.utcnow()
    )
    if alert_id:
        query = query.filter(AlertSilence.alert_id == alert_id)
    if alert_rule_id:
        query = query.filter(AlertSilence.alert_rule_id == alert_rule_id)
    return query.count()
