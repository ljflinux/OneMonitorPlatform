import logging
from typing import List, Dict, Any, Optional, Tuple
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from sqlalchemy import and_

from app.models.alert import (
    AlertRule, AlertRuleStatus, AlertRuleType, AlertSeverity,
    Alert, AlertStatus, AlertSilence
)
from app.crud import crud_alert

logger = logging.getLogger(__name__)


class AlertEngine:
    """告警引擎核心类，负责告警规则评估、告警触发与管理"""
    
    def __init__(self, db: Session):
        self.db = db
    
    def evaluate_metric_rule(
        self, rule: AlertRule, metric_data: Dict[str, float]
    ) -> Tuple[bool, Dict[str, Any]]:
        """评估指标告警规则
        
        Args:
            rule: 告警规则对象
            metric_data: 指标数据，格式为 {"metric_name": value}
            
        Returns:
            Tuple[是否触发告警, 触发详情]
        """
        try:
            metric_name = rule.condition.get("metric_name")
            if not metric_name:
                return False, {"error": "Missing metric_name in condition"}
            
            metric_value = metric_data.get(metric_name)
            if metric_value is None:
                return False, {"error": f"Metric {metric_name} not found"}
            
            # 评估阈值条件
            threshold = rule.threshold
            operator = rule.comparison_operator
            
            is_triggered = False
            if operator == ">":
                is_triggered = metric_value > threshold
            elif operator == ">=":
                is_triggered = metric_value >= threshold
            elif operator == "<":
                is_triggered = metric_value < threshold
            elif operator == "<=":
                is_triggered = metric_value <= threshold
            elif operator == "==":
                is_triggered = metric_value == threshold
            elif operator == "!=":
                is_triggered = metric_value != threshold
            else:
                return False, {"error": f"Invalid operator: {operator}"}
            
            return is_triggered, {
                "metric_name": metric_name,
                "metric_value": metric_value,
                "threshold": threshold,
                "operator": operator,
                "is_triggered": is_triggered
            }
            
        except Exception as e:
            logger.error(f"Failed to evaluate metric rule {rule.id}: {e}")
            return False, {"error": str(e)}
    
    def evaluate_log_rule(
        self, rule: AlertRule, log_data: List[Dict[str, Any]]
    ) -> Tuple[bool, Dict[str, Any]]:
        """评估日志告警规则
        
        Args:
            rule: 告警规则对象
            log_data: 日志数据列表
            
        Returns:
            Tuple[是否触发告警, 触发详情]
        """
        try:
            # 提取规则条件
            log_pattern = rule.condition.get("pattern")
            log_level = rule.condition.get("level")
            log_source = rule.condition.get("source")
            count_threshold = rule.threshold
            
            if not log_pattern and not log_level:
                return False, {"error": "Missing pattern or level in condition"}
            
            # 过滤符合条件的日志
            matching_logs = []
            for log in log_data:
                match = False
                
                # 检查日志级别
                if log_level and "level" in log:
                    if log["level"].upper() == log_level.upper():
                        match = True
                
                # 检查日志模式
                if log_pattern and "message" in log:
                    if log_pattern in log["message"]:
                        match = True
                
                # 检查日志来源
                if log_source and "source" in log:
                    if log["source"] == log_source:
                        match = True
                
                if match:
                    matching_logs.append(log)
            
            # 检查是否达到阈值
            is_triggered = len(matching_logs) >= count_threshold
            
            return is_triggered, {
                "matching_count": len(matching_logs),
                "threshold": count_threshold,
                "is_triggered": is_triggered,
                "pattern": log_pattern,
                "level": log_level,
                "source": log_source
            }
            
        except Exception as e:
            logger.error(f"Failed to evaluate log rule {rule.id}: {e}")
            return False, {"error": str(e)}
    
    def evaluate_trace_rule(
        self, rule: AlertRule, trace_data: List[Dict[str, Any]]
    ) -> Tuple[bool, Dict[str, Any]]:
        """评估链路告警规则
        
        Args:
            rule: 告警规则对象
            trace_data: 链路数据列表
            
        Returns:
            Tuple[是否触发告警, 触发详情]
        """
        try:
            # 提取规则条件
            error_rate_threshold = rule.threshold
            duration_threshold = rule.condition.get("duration_threshold")
            operation_name = rule.condition.get("operation_name")
            
            if not operation_name:
                return False, {"error": "Missing operation_name in condition"}
            
            # 统计符合条件的链路
            total_traces = 0
            error_traces = 0
            slow_traces = 0
            
            for trace in trace_data:
                if operation_name in trace.get("operation_name", ""):
                    total_traces += 1
                    
                    # 检查错误链路
                    if trace.get("status") == "ERROR":
                        error_traces += 1
                    
                    # 检查慢链路
                    if duration_threshold:
                        duration = trace.get("duration", 0)
                        if duration > duration_threshold:
                            slow_traces += 1
            
            is_triggered = False
            if total_traces > 0:
                error_rate = (error_traces / total_traces) * 100
                if error_rate >= error_rate_threshold:
                    is_triggered = True
            
            return is_triggered, {
                "total_traces": total_traces,
                "error_traces": error_traces,
                "slow_traces": slow_traces,
                "error_rate": error_rate if total_traces > 0 else 0,
                "threshold": error_rate_threshold,
                "is_triggered": is_triggered
            }
            
        except Exception as e:
            logger.error(f"Failed to evaluate trace rule {rule.id}: {e}")
            return False, {"error": str(e)}
    
    def evaluate_rule(
        self, rule: AlertRule, data: Any
    ) -> Tuple[bool, Dict[str, Any]]:
        """评估告警规则
        
        Args:
            rule: 告警规则对象
            data: 待评估的数据（指标/日志/链路）
            
        Returns:
            Tuple[是否触发告警, 触发详情]
        """
        if rule.status != AlertRuleStatus.ACTIVE:
            return False, {"error": "Rule is not active"}
        
        if rule.rule_type == AlertRuleType.METRIC:
            return self.evaluate_metric_rule(rule, data)
        elif rule.rule_type == AlertRuleType.LOG:
            return self.evaluate_log_rule(rule, data)
        elif rule.rule_type == AlertRuleType.TRACE:
            return self.evaluate_trace_rule(rule, data)
        elif rule.rule_type == AlertRuleType.CUSTOM:
            # 自定义规则评估
            return self.evaluate_custom_rule(rule, data)
        else:
            return False, {"error": f"Unknown rule type: {rule.rule_type}"}
    
    def evaluate_custom_rule(
        self, rule: AlertRule, data: Any
    ) -> Tuple[bool, Dict[str, Any]]:
        """评估自定义告警规则
        
        Args:
            rule: 告警规则对象
            data: 待评估的数据
            
        Returns:
            Tuple[是否触发告警, 触发详情]
        """
        try:
            # 自定义规则逻辑可以根据需要扩展
            logger.warning(f"Custom rule evaluation not implemented yet for rule {rule.id}")
            return False, {"error": "Custom rule evaluation not implemented"}
        except Exception as e:
            logger.error(f"Failed to evaluate custom rule {rule.id}: {e}")
            return False, {"error": str(e)}
    
    def is_silenced(self, alert_rule_id: Optional[int] = None, alert_id: Optional[int] = None) -> bool:
        """检查告警或规则是否被静默
        
        Args:
            alert_rule_id: 告警规则ID
            alert_id: 告警ID
            
        Returns:
            是否被静默
        """
        if not alert_rule_id and not alert_id:
            return False
        
        # 获取当前时间
        now = datetime.utcnow()
        
        # 构建查询条件
        conditions = [
            AlertSilence.is_active == True,
            AlertSilence.ends_at > now
        ]
        
        if alert_rule_id:
            conditions.append(AlertSilence.alert_rule_id == alert_rule_id)
        
        if alert_id:
            conditions.append(AlertSilence.alert_id == alert_id)
        
        # 执行查询
        silence = self.db.query(AlertSilence).filter(and_(*conditions)).first()
        
        return silence is not None
    
    def trigger_alert(
        self, rule: AlertRule, severity: AlertSeverity,
        source: str, source_id: Optional[str] = None,
        labels: Optional[Dict[str, Any]] = None,
        annotations: Optional[Dict[str, Any]] = None,
        ci_id: Optional[int] = None
    ) -> Optional[Alert]:
        """触发告警
        
        Args:
            rule: 告警规则对象
            severity: 告警级别
            source: 告警来源
            source_id: 来源ID
            labels: 告警标签
            annotations: 告警注释
            ci_id: 关联的CI ID
            
        Returns:
            创建的告警对象
        """
        try:
            # 检查是否被静默
            if self.is_silenced(alert_rule_id=rule.id):
                logger.info(f"Alert for rule {rule.id} is silenced, skipping")
                return None
            
            # 构建告警标题和消息
            title = f"[{severity.value.upper()}] {rule.name}"
            message = f"告警规则 {rule.name} 被触发"
            
            # 创建告警
            alert_create = {
                "alert_rule_id": rule.id,
                "title": title,
                "message": message,
                "source": source,
                "source_id": source_id,
                "labels": labels,
                "annotations": annotations,
                "ci_id": ci_id,
                "severity": severity
            }
            
            alert = crud_alert.create_alert(self.db, alert_create)
            logger.info(f"Alert triggered: {alert.id} for rule {rule.id}")
            
            return alert
            
        except Exception as e:
            logger.error(f"Failed to trigger alert for rule {rule.id}: {e}")
            return None
    
    def resolve_alert(
        self, alert_id: int, resolved_by: Optional[str] = None
    ) -> Optional[Alert]:
        """解决告警
        
        Args:
            alert_id: 告警ID
            resolved_by: 解决人
            
        Returns:
            更新后的告警对象
        """
        try:
            alert = crud_alert.resolve_alert(self.db, alert_id, resolved_by)
            if alert:
                logger.info(f"Alert resolved: {alert.id}")
            return alert
        except Exception as e:
            logger.error(f"Failed to resolve alert {alert_id}: {e}")
            return None
    
    def get_firing_alerts_by_rule(
        self, rule_id: int
    ) -> List[Alert]:
        """获取指定规则的正在触发的告警
        
        Args:
            rule_id: 告警规则ID
            
        Returns:
            正在触发的告警列表
        """
        return crud_alert.get_alerts(
            self.db, 
            alert_rule_id=rule_id,
            status=AlertStatus.FIRING
        )
    
    def evaluate_all_rules(self, data_source: str, data: Any) -> Dict[str, Any]:
        """评估所有活动告警规则
        
        Args:
            data_source: 数据来源（metric/log/trace）
            data: 待评估的数据
            
        Returns:
            评估结果统计
        """
        try:
            # 获取所有活动规则
            rules = crud_alert.get_alert_rules(
                self.db, 
                status=AlertRuleStatus.ACTIVE
            )
            
            # 过滤与数据源匹配的规则
            rule_type = None
            if data_source == "metric":
                rule_type = AlertRuleType.METRIC
            elif data_source == "log":
                rule_type = AlertRuleType.LOG
            elif data_source == "trace":
                rule_type = AlertRuleType.TRACE
            
            if rule_type:
                rules = [r for r in rules if r.rule_type == rule_type]
            
            # 统计信息
            total_rules = len(rules)
            evaluated_rules = 0
            triggered_rules = 0
            triggered_alerts = 0
            
            # 评估每个规则
            for rule in rules:
                try:
                    is_triggered, details = self.evaluate_rule(rule, data)
                    evaluated_rules += 1
                    
                    if is_triggered:
                        triggered_rules += 1
                        
                        # 检查是否已有相同规则的触发告警
                        firing_alerts = self.get_firing_alerts_by_rule(rule.id)
                        if not firing_alerts:
                            # 创建新告警
                            alert = self.trigger_alert(
                                rule=rule,
                                severity=rule.severity,
                                source=data_source,
                                labels=details
                            )
                            if alert:
                                triggered_alerts += 1
                            
                    else:
                        # 解决该规则的所有触发告警
                        firing_alerts = self.get_firing_alerts_by_rule(rule.id)
                        for alert in firing_alerts:
                            self.resolve_alert(alert.id)
                            
                except Exception as e:
                    logger.error(f"Failed to process rule {rule.id}: {e}")
            
            return {
                "total_rules": total_rules,
                "evaluated_rules": evaluated_rules,
                "triggered_rules": triggered_rules,
                "triggered_alerts": triggered_alerts,
                "timestamp": datetime.utcnow().isoformat()
            }
            
        except Exception as e:
            logger.error(f"Failed to evaluate all rules: {e}")
            return {
                "error": str(e),
                "timestamp": datetime.utcnow().isoformat()
            }
    
    def cleanup_old_alerts(self, retention_days: int = 90) -> Dict[str, int]:
        """清理旧告警数据
        
        Args:
            retention_days: 保留天数
            
        Returns:
            清理结果统计
        """
        try:
            cutoff_time = datetime.utcnow() - timedelta(days=retention_days)
            
            # 获取要删除的告警
            old_alerts = self.db.query(Alert).filter(
                Alert.created_at < cutoff_time,
                Alert.status != AlertStatus.FIRING
            ).all()
            
            deleted_count = 0
            for alert in old_alerts:
                try:
                    self.db.delete(alert)
                    deleted_count += 1
                except Exception as e:
                    logger.error(f"Failed to delete alert {alert.id}: {e}")
                    continue
            
            self.db.commit()
            
            return {
                "deleted_alerts": deleted_count,
                "cutoff_time": cutoff_time.isoformat(),
                "retention_days": retention_days
            }
            
        except Exception as e:
            logger.error(f"Failed to cleanup old alerts: {e}")
            return {
                "error": str(e),
                "deleted_alerts": 0
            }


def get_alert_engine(db: Session) -> AlertEngine:
    """获取告警引擎实例
    
    Args:
        db: 数据库会话
        
    Returns:
        告警引擎实例
    """
    return AlertEngine(db)
