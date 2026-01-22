package io.onemonitor.cmdb.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.onemonitor.cmdb.domain.event.CIRelationshipCreated;
import io.onemonitor.cmdb.domain.model.identifier.CIId;
import io.onemonitor.cmdb.domain.model.identifier.CIRelationId;
import io.onemonitor.common.domain.base.AggregateRoot;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.*;

/**
 * CI Relation Aggregate Root.
 * 
 * Invariants:
 * 1. Cannot create self-referential relations (A cannot relate to itself)
 * 2. Cannot create circular dependencies
 * 3. Relation has temporal validity (validFrom, validTo)
 * 
 * Boundary: Single relation and its attributes
 */
@Entity
@Table(name = "ci_relations", indexes = {
    @Index(name = "idx_relation_source", columnList = "source_ci_id"),
    @Index(name = "idx_relation_target", columnList = "target_ci_id"),
    @Index(name = "idx_relation_type", columnList = "relation_type")
})
public class CIRelation extends AggregateRoot<CIRelationId> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private CIRelationId id;

    @Column(name = "source_ci_id", nullable = false, length = 36)
    private CIId sourceCiId;

    @Column(name = "target_ci_id", nullable = false, length = 36)
    private CIId targetCiId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 50)
    private CIRelationType type;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @JdbcTypeCode(JsonType.NAME)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String createdBy;

    // Protected constructor for JPA
    protected CIRelation() {
        super();
    }

    /**
     * Creates a new CI relation.
     * 
     * @param sourceCiId The source CI ID
     * @param targetCiId The target CI ID
     * @param type The relation type
     * @param attributes Additional attributes
     * @param createdBy The user creating the relation
     * @return The new CIRelation
     */
    public static CIRelation create(CIId sourceCiId, CIId targetCiId, CIRelationType type, 
                                   Map<String, Object> attributes, String createdBy) {
        // Invariant: Cannot self-reference
        if (sourceCiId.getValue().equals(targetCiId.getValue())) {
            throw new SelfReferenceException("CI cannot relate to itself: " + sourceCiId.getValue());
        }

        CIRelation relation = new CIRelation();
        relation.id = CIRelationId.generate();
        relation.sourceCiId = sourceCiId;
        relation.targetCiId = targetCiId;
        relation.type = Objects.requireNonNull(type, "Relation type cannot be null");
        relation.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
        relation.validFrom = Instant.now();
        relation.createdBy = createdBy;
        relation.createdAt = Instant.now();
        relation.updatedAt = relation.createdAt;

        // Raise domain event
        relation.addDomainEvent(new CIRelationshipCreated(
            relation.id, sourceCiId, targetCiId, type, relation.createdAt
        ));

        return relation;
    }

    /**
     * Invalidates this relation (soft delete).
     */
    public void invalidate(String reason) {
        this.validTo = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the relation attributes.
     */
    public void updateAttributes(Map<String, Object> newAttributes) {
        this.attributes = new HashMap<>(Objects.requireNonNull(newAttributes, "Attributes cannot be null"));
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the description.
     */
    public void updateDescription(String newDescription) {
        this.description = newDescription;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if this relation is currently valid.
     */
    public boolean isValid() {
        Instant now = Instant.now();
        return (validFrom == null || !validFrom.isAfter(now)) &&
               (validTo == null || !validTo.isBefore(now));
    }

    /**
     * Checks if this relation involves the given CI.
     */
    public boolean involves(CIId ciId) {
        return sourceCiId.equals(ciId) || targetCiId.equals(ciId);
    }

    // Getters
    @Override
    public CIRelationId getId() {
        return this.id;
    }

    public CIId getSourceCiId() {
        return sourceCiId;
    }

    public CIId getTargetCiId() {
        return targetCiId;
    }

    public CIRelationType getType() {
        return type;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public Map<String, Object> getAttributes() {
        return attributes != null ? Collections.unmodifiableMap(new HashMap<>(attributes)) : Collections.emptyMap();
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    // Setters for JPA
    public void setId(CIRelationId id) {
        this.id = id;
    }

    public void setSourceCiId(CIId sourceCiId) {
        this.sourceCiId = sourceCiId;
    }

    public void setTargetCiId(CIId targetCiId) {
        this.targetCiId = targetCiId;
    }

    public void setType(CIRelationType type) {
        this.type = type;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Enumeration of CI relation types.
     */
    public enum CIRelationType {
        CONTAINS("contains", "包含", "A contains B (e.g., a rack contains servers)"),
        DEPENDS_ON("depends_on", "依赖", "A depends on B (e.g., an app depends on a database)"),
        RUNS_ON("runs_on", "部署在", "A runs on B (e.g., an app runs on a server)"),
        CONNECTED_TO("connected_to", "连接", "A is connected to B (e.g., a server is connected to a switch)"),
        MANAGES("manages", "管理", "A manages B (e.g., a load balancer manages servers)"),
        PART_OF("part_of", "属于", "A is part of B (e.g., a disk is part of a server)"),
        REPLICATES_TO("replicates_to", "复制到", "A replicates to B (e.g., primary DB replicates to standby)");

        private final String code;
        private final String chineseName;
        private final String description;

        CIRelationType(String code, String chineseName, String description) {
            this.code = code;
            this.chineseName = chineseName;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getChineseName() {
            return chineseName;
        }

        public String getDescription() {
            return description;
        }

        public static CIRelationType fromCode(String code) {
            for (CIRelationType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown relation type code: " + code);
        }

        /**
         * Returns true if this relation type creates a directed dependency.
         * For example, DEPENDS_ON means A depends on B, so B is a dependency of A.
         */
        public boolean createsDependency() {
            return this == DEPENDS_ON || this == RUNS_ON || this == REPLICATES_TO;
        }

        /**
         * Returns the inverse relation type.
         * For example, the inverse of CONTAINS is PART_OF.
         */
        public CIRelationType inverse() {
            return switch (this) {
                case CONTAINS -> PART_OF;
                case DEPENDS_ON -> MANAGES;
                case RUNS_ON -> HOSTS;
                case CONNECTED_TO -> CONNECTED_TO;
                case MANAGES -> DEPENDS_ON;
                case PART_OF -> CONTAINS;
                case REPLICATES_TO -> REPLICATES_FROM;
            };
        }

        private static final CIRelationType HOSTS = new CIRelationType("hosts", "托管", "A hosts B");
        private static final CIRelationType REPLICATES_FROM = new CIRelationType("replicates_from", "从...复制", "A replicates from B");
    }

    // Exception classes
    public static class SelfReferenceException extends RuntimeException {
        public SelfReferenceException(String message) {
            super(message);
        }
    }

    public static class CircularDependencyException extends RuntimeException {
        public CircularDependencyException(CIId source, CIId target) {
            super("Creating relation from " + source.getValue() + " to " + target.getValue() + " would create a circular dependency");
        }
    }
}
