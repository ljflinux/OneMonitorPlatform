package io.onemonitor.cmdb.domain.event;

import io.onemonitor.cmdb.domain.model.CILifecycleStatus;
import io.onemonitor.cmdb.domain.model.identifier.CIId;
import io.onemonitor.cmdb.domain.model.identifier.CITypeId;
import io.onemonitor.common.domain.event.DomainEvent;
import io.onemonitor.common.domain.vo.LabelSet;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Base interface for CMDB domain events.
 */
public interface CMDBEvent extends DomainEvent {
    CIId ciId();
    Instant timestamp();
    
    @Override
    default String getAggregateId() {
        return ciId().getValue();
    }
    
    @Override
    default String getAggregateType() {
        return "CI";
    }
}

/**
 * Event raised when a new CI is created.
 */
public record CICreated(
    CIId ciId,
    CITypeId typeId,
    String name,
    LabelSet labels,
    Instant timestamp
) implements CMDBEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "CICreated";
    }
}

/**
 * Event raised when CI attributes are updated.
 */
public record CIUpdated(
    CIId ciId,
    CITypeId typeId,
    Map<String, Object> oldValues,
    Map<String, Object> newValues,
    Set<String> changedFields,
    Instant timestamp
) implements CMDBEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "CIUpdated";
    }
}

/**
 * Event raised when CI status changes.
 */
public record CIStatusChanged(
    CIId ciId,
    CILifecycleStatus oldStatus,
    CILifecycleStatus newStatus,
    String reason,
    Instant timestamp
) implements CMDBEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "CIStatusChanged";
    }
}

/**
 * Event raised when a CI is decommissioned.
 */
public record CIDecommissioned(
    CIId ciId,
    String reason,
    List<CIId> affectedDependents,
    Instant timestamp
) implements CMDBEvent {
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "CIDecommissioned";
    }
}
