package io.onemonitor.cmdb.domain.event;

import io.onemonitor.cmdb.domain.model.CIRelation.CIRelationType;
import io.onemonitor.cmdb.domain.model.identifier.CIId;
import io.onemonitor.cmdb.domain.model.identifier.CIRelationId;
import io.onemonitor.common.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Event raised when a CI relationship is created.
 */
public record CIRelationshipCreated(
    CIRelationId relationId,
    CIId sourceCiId,
    CIId targetCiId,
    CIRelationType type,
    Instant timestamp
) implements DomainEvent {
    
    @Override
    public String getAggregateId() {
        return relationId.getValue();
    }
    
    @Override
    public String getAggregateType() {
        return "CIRelation";
    }
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "CIRelationshipCreated";
    }
    
    @Override
    public Instant getOccurredAt() {
        return timestamp;
    }
}

/**
 * Event raised when a CI relationship is deleted.
 */
public record CIRelationshipDeleted(
    CIRelationId relationId,
    CIId sourceCiId,
    CIId targetCiId,
    CIRelationType type,
    String reason,
    Instant timestamp
) implements DomainEvent {
    
    @Override
    public String getAggregateId() {
        return relationId.getValue();
    }
    
    @Override
    public String getAggregateType() {
        return "CIRelation";
    }
    
    @Override
    public String getEventId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String getEventType() {
        return "CIRelationshipDeleted";
    }
    
    @Override
    public Instant getOccurredAt() {
        return timestamp;
    }
}
