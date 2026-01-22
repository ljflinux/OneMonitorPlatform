package io.onemonitor.cmdb.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.onemonitor.cmdb.domain.model.identifier.CIId;
import io.onemonitor.cmdb.domain.model.identifier.CITypeId;
import io.onemonitor.common.domain.base.AggregateRoot;
import io.onemonitor.common.domain.event.DomainEvent;
import io.onemonitor.common.domain.vo.LabelSet;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.*;

/**
 * CI (Configuration Item) Aggregate Root.
 * 
 * Invariants:
 * 1. CI status transitions must follow the state machine rules
 * 2. CI attributes must conform to the CIType schema definition
 * 3. CI name must be unique within the same CIType
 * 4. Decommissioned CIs cannot be updated
 * 
 * Boundary: CI and its attributes, labels (transaction consistency)
 */
@Entity
@Table(name = "cis", indexes = {
    @Index(name = "idx_ci_type_id", columnList = "type_id"),
    @Index(name = "idx_ci_name", columnList = "name"),
    @Index(name = "idx_ci_status", columnList = "status"),
    @Index(name = "idx_ci_owner", columnList = "owner")
})
public class CI extends AggregateRoot<CIId> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private CIId id;

    @Column(name = "type_id", nullable = false, length = 36)
    private CITypeId typeId;

    /**
     * CI unique identifier within its type scope.
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Human-readable display name.
     */
    @Column(name = "display_name", length = 500)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CILifecycleStatus status;

    /**
     * CI type-defined attributes stored as JSONB.
     */
    @JdbcTypeCode(JsonType.NAME)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    /**
     * Monitoring correlation labels for Prometheus/Loki/Tempo integration.
     */
    @JdbcTypeCode(JsonType.NAME)
    @Column(name = "labels", columnDefinition = "jsonb")
    private LabelSet labels;

    // Metadata fields
    @Column(length = 255)
    private String owner;

    @Column(length = 255)
    private String department;

    @Column(length = 255)
    private String location;

    @Column(name = "environment", length = 50)
    private String environment;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    // Protected constructor for JPA
    protected CI() {
        super();
    }

    /**
     * Factory method to create a new CI.
     */
    public static CI create(CITypeId typeId, String name, Map<String, Object> attributes, 
                            LabelSet labels, String owner, String environment) {
        CI ci = new CI();
        ci.id = CIId.generate();
        ci.typeId = typeId;
        ci.name = validateName(name);
        ci.displayName = name;
        ci.attributes = new HashMap<>(Objects.requireNonNull(attributes, "Attributes cannot be null"));
        ci.labels = labels != null ? labels : LabelSet.empty();
        ci.owner = owner;
        ci.environment = environment;
        ci.status = CILifecycleStatus.ACTIVE;
        ci.createdAt = Instant.now();
        ci.updatedAt = ci.createdAt;

        // Validate attributes against CIType schema (would be injected via domain service)
        // ci.validateAttributes();

        // Raise domain event
        ci.addDomainEvent(new CICreated(
            ci.id, ci.typeId, ci.name, ci.labels, ci.createdAt
        ));

        return ci;
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("CI name cannot be null or blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("CI name cannot exceed 255 characters");
        }
        // Name should be alphanumeric with dashes/underscores
        if (!name.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("CI name must be alphanumeric with dashes/underscores only");
        }
        return name.toLowerCase();
    }

    /**
     * Updates the CI attributes.
     * 
     * Invariant: Decommissioned CIs cannot be updated.
     */
    public void updateAttributes(Map<String, Object> newAttributes) {
        if (this.status == CILifecycleStatus.DECOMMISSIONED || 
            this.status == CILifecycleStatus.DISPOSED) {
            throw new CannotUpdateTerminatedCIException(this.id, this.status);
        }

        Map<String, Object> oldAttributes = new HashMap<>(this.attributes);
        this.attributes = new HashMap<>(Objects.requireNonNull(newAttributes, "Attributes cannot be null"));

        // Validate against CIType schema (would be injected via domain service)
        // this.validateAttributes();

        this.updatedAt = Instant.now();

        // Raise domain event
        this.addDomainEvent(new CIUpdated(
            this.id, this.typeId, oldAttributes, this.attributes, 
            newAttributes.keySet(), this.updatedAt
        ));
    }

    /**
     * Changes the CI status following the state machine rules.
     */
    public void changeStatus(CILifecycleStatus newStatus, String reason) {
        if (this.status == newStatus) {
            return; // No change
        }

        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(this.id, this.status, newStatus);
        }

        CILifecycleStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();

        // Raise domain event
        this.addDomainEvent(new CIStatusChanged(
            this.id, oldStatus, newStatus, reason, this.updatedAt
        ));

        // If decommissioned, trigger dependent CI status check
        if (newStatus == CILifecycleStatus.DECOMMISSIONED) {
            this.addDomainEvent(new CIDecommissioned(
                this.id, reason, Collections.emptyList(), this.updatedAt
            ));
        }
    }

    /**
     * Puts the CI into maintenance mode.
     */
    public void enterMaintenance(String reason) {
        changeStatus(CILifecycleStatus.MAINTENANCE, reason);
    }

    /**
     * Returns the CI to active status from maintenance.
     */
    public void returnToActive(String reason) {
        changeStatus(CILifecycleStatus.ACTIVE, reason);
    }

    /**
     * Decommission the CI.
     */
    public void decommission(String reason) {
        changeStatus(CILifecycleStatus.DECOMMISSIONED, reason);
    }

    /**
     * Dispose the CI data (archive/purge).
     */
    public void dispose(String reason) {
        changeStatus(CILifecycleStatus.DISPOSED, reason);
    }

    /**
     * Updates the display name.
     */
    public void updateDisplayName(String newDisplayName) {
        if (this.status == CILifecycleStatus.DECOMMISSIONED || 
            this.status == CILifecycleStatus.DISPOSED) {
            throw new CannotUpdateTerminatedCIException(this.id, this.status);
        }
        this.displayName = newDisplayName;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the owner.
     */
    public void updateOwner(String newOwner) {
        if (this.status == CILifecycleStatus.DECOMMISSIONED || 
            this.status == CILifecycleStatus.DISPOSED) {
            throw new CannotUpdateTerminatedCIException(this.id, this.status);
        }
        this.owner = newOwner;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the monitoring labels.
     */
    public void updateLabels(LabelSet newLabels) {
        if (this.status == CILifecycleStatus.DECOMMISSIONED || 
            this.status == CILifecycleStatus.DISPOSED) {
            throw new CannotUpdateTerminatedCIException(this.id, this.status);
        }
        this.labels = newLabels;
        this.updatedAt = Instant.now();
    }

    /**
     * Returns correlation labels for monitoring data association.
     * These labels are used to correlate CI with metrics, logs, and traces.
     */
    public LabelSet getCorrelationLabels() {
        Map<String, String> correlationLabels = new HashMap<>();
        correlationLabels.put("ci_id", this.id.getValue());
        correlationLabels.put("ci_name", this.name);
        correlationLabels.put("ci_type", this.typeId.getValue());
        
        if (this.environment != null) {
            correlationLabels.put("environment", this.environment);
        }
        if (this.owner != null) {
            correlationLabels.put("owner", this.owner);
        }
        if (this.location != null) {
            correlationLabels.put("location", this.location);
        }

        // Merge with existing labels
        if (this.labels != null && !this.labels.isEmpty()) {
            correlationLabels.putAll(this.labels.asMap());
        }

        return new LabelSet(correlationLabels);
    }

    /**
     * Returns a label for exact matching in monitoring queries.
     */
    public LabelSet getExactMatchLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("ci_id", this.id.getValue());
        return new LabelSet(labels);
    }

    // Getters
    @Override
    public CIId getId() {
        return this.id;
    }

    public CITypeId getTypeId() {
        return typeId;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CILifecycleStatus getStatus() {
        return status;
    }

    public Map<String, Object> getAttributes() {
        return attributes != null ? Collections.unmodifiableMap(new HashMap<>(attributes)) : Collections.emptyMap();
    }

    public LabelSet getLabels() {
        return labels != null ? labels : LabelSet.empty();
    }

    public String getOwner() {
        return owner;
    }

    public String getDepartment() {
        return department;
    }

    public String getLocation() {
        return location;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    // Setters for JPA
    public void setId(CIId id) {
        this.id = id;
    }

    public void setTypeId(CITypeId typeId) {
        this.typeId = typeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setStatus(CILifecycleStatus status) {
        this.status = status;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setLabels(LabelSet labels) {
        this.labels = labels;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Exception classes
    public static class CannotUpdateTerminatedCIException extends DomainException {
        public CannotUpdateTerminatedCIException(CIId ciId, CILifecycleStatus status) {
            super("Cannot update CI " + ciId.getValue() + " in status " + status);
        }
    }

    public static class InvalidStatusTransitionException extends DomainException {
        public InvalidStatusTransitionException(CIId ciId, CILifecycleStatus from, CILifecycleStatus to) {
            super("Invalid status transition for CI " + ciId.getValue() + " from " + from + " to " + to);
        }
    }

    public static abstract class DomainException extends RuntimeException {
        public DomainException(String message) {
            super(message);
        }
    }
}
