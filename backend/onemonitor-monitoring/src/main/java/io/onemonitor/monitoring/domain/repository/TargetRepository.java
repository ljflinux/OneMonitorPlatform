package io.onemonitor.monitoring.domain.repository;

import io.onemonitor.monitoring.domain.model.Target;
import io.onemonitor.monitoring.domain.model.Target.TargetStatus;
import io.onemonitor.monitoring.domain.model.Target.TargetType;
import io.onemonitor.monitoring.domain.model.identifier.TargetId;
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
 * Repository interface for Target Aggregate.
 */
public interface TargetRepository {

    /**
     * Saves a target.
     */
    Target save(Target target);

    /**
     * Finds a target by ID.
     */
    Optional<Target> findById(TargetId id);

    /**
     * Finds all targets.
     */
    List<Target> findAll();

    /**
     * Finds targets by IDs.
     */
    List<Target> findByIdIn(Collection<TargetId> ids);

    /**
     * Finds targets by type.
     */
    List<Target> findByType(TargetType type);

    /**
     * Finds targets by type with pagination.
     */
    Page<Target> findByType(TargetType type, Pageable pageable);

    /**
     * Finds targets by status.
     */
    List<Target> findByStatus(TargetStatus status);

    /**
     * Finds targets by status with pagination.
     */
    Page<Target> findByStatus(TargetStatus status, Pageable pageable);

    /**
     * Finds targets by CI ID.
     */
    List<Target> findByCiId(String ciId);

    /**
     * Finds enabled targets that need scraping.
     */
    @Query("SELECT t FROM Target t WHERE t.enabled = true AND " +
           "(t.nextScrape IS NULL OR t.nextScrape <= :now)")
    List<Target> findTargetsNeedingScrape(@Param("now") Instant now);

    /**
     * Finds targets by endpoint.
     */
    Optional<Target> findByEndpoint(String endpoint);

    /**
     * Checks if a target exists with the given endpoint.
     */
    boolean existsByEndpoint(String endpoint);

    /**
     * Finds targets that have been unhealthy for too long.
     */
    @Query("SELECT t FROM Target t WHERE t.status IN :statuses AND " +
           "t.lastScrapeFailure < :threshold")
    List<Target> findLongUnhealthyTargets(
        @Param("statuses") Collection<TargetStatus> statuses,
        @Param("threshold") Instant threshold
    );

    /**
     * Counts targets by type.
     */
    long countByType(TargetType type);

    /**
     * Counts targets by status.
     */
    long countByStatus(TargetStatus status);

    /**
     * Deletes a target by ID.
     */
    void deleteById(TargetId id);

    /**
     * Finds targets with custom query.
     */
    @Query("SELECT t FROM Target t WHERE " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:ciId IS NULL OR t.ciId = :ciId) AND " +
           "(:enabled IS NULL OR t.enabled = :enabled)")
    Page<Target> findByFilters(
        @Param("type") TargetType type,
        @Param("status") TargetStatus status,
        @Param("ciId") String ciId,
        @Param("enabled") Boolean enabled,
        Pageable pageable
    );

    /**
     * Searches targets by name.
     */
    @Query("SELECT t FROM Target t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.endpoint) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Target> searchByNameOrEndpoint(
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
}
