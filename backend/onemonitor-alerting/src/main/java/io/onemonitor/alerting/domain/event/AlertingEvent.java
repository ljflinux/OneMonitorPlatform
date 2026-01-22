package io.onemonitor.alerting.domain.event;

import io.onemonitor.alerting.domain.model.AlertInstance.AlertStatus;
import io.onemonitor.alerting.domain.model.AlertRule.AlertSeverity;
import io.onemonitor.alerting.domain.model.identifier.AlertInstanceId;
import io.onemonitor.alerting.domain.model.identifier.AlertRuleId;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Base interface for Alerting domain events.
 */
public interface AlertingEvent {
    AlertRuleId ruleId();
    Instant timestamp();
    
    default String getAggregateId() {
        return ruleId().getValue();
    }
    
    default String getAggregateType() {
        return "AlertRule";
    }
}

/**
 * Event raised when an alert rule is created.
 */
public record AlertRuleCreated(
    AlertRuleId ruleId,
    String name,
    AlertSeverity severity,
    String ciId,
    Instant timestamp
) implements AlertingEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "AlertRuleCreated";
    }
}

/**
 * Event raised when an alert rule is modified.
 */
public record AlertRuleModified(
    AlertRuleId ruleId,
    String name,
    String modifiedFields,
    Instant timestamp
) implements AlertingEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "AlertRuleModified";
    }
}

/**
 * Event raised when an alert rule is enabled/disabled.
 */
public record AlertRuleEnabledChanged(
    AlertRuleId ruleId,
    String name,
    boolean enabled,
    Instant timestamp
) implements AlertingEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "AlertRuleEnabledChanged";
    }
}

/**
 * Event raised when an alert fires.
 */
public record AlertFired(
    AlertInstanceId alertId,
    AlertRuleId ruleId,
    String ruleName,
    AlertSeverity severity,
    String ciId,
    Map<String, String> labels,
    double currentValue,
    String summary,
    Instant timestamp
) implements AlertingEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "AlertFired";
    }
}

/**
 * Event raised when an alert is acknowledged.
 */
public record AlertAcknowledged(
    AlertInstanceId alertId,
    AlertRuleId ruleId,
    String ruleName,
    String acknowledgedBy,
    String comment,
    Instant timestamp
) implements AlertingEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "AlertAcknowledged";
    }
}

/**
 * Event raised when an alert is resolved.
 */
public record AlertResolved(
    AlertInstanceId alertId,
    AlertRuleId ruleId,
    String ruleName,
    String resolvedBy,
    String comment,
    boolean autoResolved,
    long durationSeconds,
    Instant timestamp
) implements AlertingEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "AlertResolved";
    }
}

/**
 * Event raised when an alert is escalated.
 */
public record AlertEscalated(
    AlertInstanceId alertId,
    AlertRuleId ruleId,
    String ruleName,
    AlertSeverity severity,
    String previousAssignee,
    String escalatedTo,
    String reason,
    Instant timestamp
) implements AlertingEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "AlertEscalated";
    }
}

/**
 * Event raised for notification delivery.
 */
public record NotificationSent(
    AlertInstanceId alertId,
    AlertRuleId ruleId,
    String channel,
    String channelType,
    boolean success,
    String errorMessage,
    Instant timestamp
) implements AlertingEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "NotificationSent";
    }
}
