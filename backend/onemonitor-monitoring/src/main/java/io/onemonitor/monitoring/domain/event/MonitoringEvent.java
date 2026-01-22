package io.onemonitor.monitoring.domain.event;

import io.onemonitor.monitoring.domain.model.Target.TargetStatus;
import io.onemonitor.monitoring.domain.model.Target.TargetType;
import io.onemonitor.monitoring.domain.model.identifier.TargetId;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Base interface for Monitoring domain events.
 */
public interface MonitoringEvent {
    TargetId targetId();
    Instant timestamp();
    
    default String getAggregateId() {
        return targetId().getValue();
    }
    
    default String getAggregateType() {
        return "Target";
    }
}

/**
 * Event raised when a new target is created.
 */
public record TargetCreated(
    TargetId targetId,
    String name,
    TargetType type,
    String endpoint,
    String ciId,
    Instant timestamp
) implements MonitoringEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "TargetCreated";
    }
}

/**
 * Event raised when target status changes.
 */
public record TargetStatusChanged(
    TargetId targetId,
    String name,
    TargetStatus oldStatus,
    TargetStatus newStatus,
    String reason,
    Instant timestamp
) implements MonitoringEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "TargetStatusChanged";
    }
}

/**
 * Event raised when a scrape succeeds.
 */
public record ScrapeSuccess(
    TargetId targetId,
    String endpoint,
    int metricsCount,
    Instant timestamp
) implements MonitoringEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "ScrapeSuccess";
    }
}

/**
 * Event raised when a scrape fails.
 */
public record ScrapeFailure(
    TargetId targetId,
    String endpoint,
    String errorMessage,
    Instant timestamp
) implements MonitoringEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "ScrapeFailure";
    }
}

/**
 * Event raised when a new metric series is discovered.
 */
public record MetricDiscovered(
    TargetId targetId,
    String metricName,
    Map<String, String> labels,
    Instant timestamp
) implements MonitoringEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "MetricDiscovered";
    }
}

/**
 * Event raised when metric anomaly is detected.
 */
public record MetricAnomalyDetected(
    TargetId targetId,
    String metricName,
    String description,
    Instant timestamp
) implements MonitoringEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "MetricAnomalyDetected";
    }
}
