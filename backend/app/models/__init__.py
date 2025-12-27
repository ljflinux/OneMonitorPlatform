# Import all models here for easier access
from app.models.user import User
from app.models.monitor import MonitorObject, MonitorMetric, MonitorConfig
from app.models.alert import AlertRule, Alert, AlertGroup, AlertHistory
from app.models.cmdb import (
    CILifecycleStatus,
    RelationType,
    CI_Type,
    CI_Attribute,
    CI,
    CI_Data,
    CI_Relation,
    CI_Change_History
)
