package io.onemonitor.common.domain.base;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Base class for all Aggregate Roots in the domain.
 * 
 * Aggregate Roots are the entry points to aggregates and ensure
 * consistency boundaries are maintained.
 */
@MappedSuperclass
public abstract class AggregateRoot<ID extends Identifier> implements Entity<ID> {

    private static final long serialVersionUID = 1L;

    @Transient
    private final Set<DomainEvent> domainEvents = new HashSet<>();

    @Override
    public abstract ID getId();

    /**
     * Adds a domain event to be published after the aggregate is persisted.
     */
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Removes a domain event.
     */
    protected void removeDomainEvent(DomainEvent event) {
        this.domainEvents.remove(event);
    }

    /**
     * Returns an unmodifiable set of domain events.
     */
    public Set<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableSet(this.domainEvents);
    }

    /**
     * Clears all domain events after they have been published.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * Returns the version of the aggregate for optimistic locking.
     */
    @Version
    private Long version;

    /**
     * Returns the creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Returns the last update timestamp.
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Generates a new UUID-based ID.
     */
    protected static String newId() {
        return UUID.randomUUID().toString();
    }
}
