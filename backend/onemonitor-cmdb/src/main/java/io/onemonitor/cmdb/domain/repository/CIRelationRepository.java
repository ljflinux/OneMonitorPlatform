package io.onemonitor.cmdb.domain.repository;

import io.onemonitor.cmdb.domain.model.CIRelation;
import io.onemonitor.cmdb.domain.model.CIRelation.CIRelationType;
import io.onemonitor.cmdb.domain.model.identifier.CIId;
import io.onemonitor.cmdb.domain.model.identifier.CIRelationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CI Relation Aggregate.
 * 
 * This interface defines the contract for CI Relation persistence operations.
 * Implementation should be in the infrastructure layer.
 */
public interface CIRelationRepository {

    /**
     * Saves a CI relation.
     */
    CIRelation save(CIRelation relation);

    /**
     * Finds a relation by its ID.
     */
    Optional<CIRelation> findById(CIRelationId id);

    /**
     * Finds all relations.
     */
    List<CIRelation> findAll();

    /**
     * Finds relations by their IDs.
     */
    List<CIRelation> findByIdIn(Collection<CIRelationId> ids);

    /**
     * Finds relations by source CI ID.
     */
    List<CIRelation> findBySourceCiId(CIId sourceCiId);

    /**
     * Finds relations by source CI ID with pagination.
     */
    Page<CIRelation> findBySourceCiId(CIId sourceCiId, Pageable pageable);

    /**
     * Finds relations by target CI ID.
     */
    List<CIRelation> findByTargetCiId(CIId targetCiId);

    /**
     * Finds relations by target CI ID with pagination.
     */
    Page<CIRelation> findByTargetCiId(CIId targetCiId, Pageable pageable);

    /**
     * Finds all relations involving a CI (either as source or target).
     */
    List<CIRelation> findBySourceCiIdOrTargetCiId(CIId ciId);

    /**
     * Finds all relations involving a CI with pagination.
     */
    Page<CIRelation> findBySourceCiIdOrTargetCiId(CIId ciId, Pageable pageable);

    /**
     * Finds relations by type.
     */
    List<CIRelation> findByType(CIRelationType type);

    /**
     * Finds relations by type with pagination.
     */
    Page<CIRelation> findByType(CIRelationType type, Pageable pageable);

    /**
     * Finds a specific relation between two CIs.
     */
    Optional<CIRelation> findBySourceCiIdAndTargetCiIdAndType(
        CIId sourceCiId, CIId targetCiId, CIRelationType type);

    /**
     * Checks if a relation exists between two CIs.
     */
    boolean existsBySourceCiIdAndTargetCiIdAndType(
        CIId sourceCiId, CIId targetCiId, CIRelationType type);

    /**
     * Finds all upstream dependencies (CIs that the given CI depends on).
     */
    @Query("SELECT r FROM CIRelation r WHERE r.sourceCiId = :ciId AND r.type IN :dependencyTypes")
    List<CIRelation> findUpstreamDependencies(
        @Param("ciId") CIId ciId,
        @Param("dependencyTypes") Collection<CIRelationType> dependencyTypes);

    /**
     * Finds all downstream dependents (CIs that depend on the given CI).
     */
    @Query("SELECT r FROM CIRelation r WHERE r.targetCiId = :ciId AND r.type IN :dependencyTypes")
    List<CIRelation> findDownstreamDependents(
        @Param("ciId") CIId ciId,
        @Param("dependencyTypes") Collection<CIRelationType> dependencyTypes);

    /**
     * Finds all valid relations (currently active).
     */
    @Query("SELECT r FROM CIRelation r WHERE " +
           "(r.validFrom IS NULL OR r.validFrom <= :now) AND " +
           "(r.validTo IS NULL OR r.validTo > :now)")
    List<CIRelation> findAllValid(@Param("now") Instant now);

    /**
     * Finds all valid relations involving a CI.
     */
    @Query("SELECT r FROM CIRelation r WHERE " +
           "(r.sourceCiId = :ciId OR r.targetCiId = :ciId) AND " +
           "(r.validFrom IS NULL OR r.validFrom <= :now) AND " +
           "(r.validTo IS NULL OR r.validTo > :now)")
    List<CIRelation> findValidRelationsInvolving(
        @Param("ciId") CIId ciId,
        @Param("now") Instant now);

    /**
     * Deletes a relation by ID.
     */
    void deleteById(CIRelationId id);

    /**
     * Soft deletes a relation (sets validTo to now).
     */
    void invalidateById(CIRelationId id);

    /**
     * Counts relations by source CI ID.
     */
    long countBySourceCiId(CIId sourceCiId);

    /**
     * Counts relations by target CI ID.
     */
    long countByTargetCiId(CIId targetCiId);

    /**
     * Counts relations by type.
     */
    long countByType(CIRelationType type);

    /**
     * Finds relations with custom query.
     */
    @Query("SELECT r FROM CIRelation r WHERE " +
           "(:sourceCiId IS NULL OR r.sourceCiId = :sourceCiId) AND " +
           "(:targetCiId IS NULL OR r.targetCiId = :targetCiId) AND " +
           "(:type IS NULL OR r.type = :type)")
    Page<CIRelation> findByFilters(
        @Param("sourceCiId") CIId sourceCiId,
        @Param("targetCiId") CIId targetCiId,
        @Param("type") CIRelationType type,
        Pageable pageable
    );
}
