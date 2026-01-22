package io.onemonitor.common.domain.event;

import java.time.Instant;

/**
 * Base interface for all domain events.
 * 
 * Domain events represent something that happened in the domain
 * that other parts of the system might be interested in.
 */
public interface DomainEvent {

    /**
     * Returns the unique identifier of the aggregate that raised this event.
     */
    String getAggregateId();

    /**
     * Returns the type of the aggregate that raised this event.
     */
    String getAggregateType();

    /**
     * Returns the timestamp when this event occurred.
     */
    Instant getOccurredAt();

    /**
     * Returns the version of the event schema for compatibility.
     */
    default int getVersion() {
        return 1;
    }

    /**
     * Returns a unique identifier for this event.
     */
    String getEventId();

    /**
     * Returns the type name of this event.
     */
    String getEventType();
}
