package io.onemonitor.cmdb.domain.repository;

import io.onemonitor.cmdb.domain.model.CI;
import io.onemonitor.cmdb.domain.model.CILifecycleStatus;
import io.onemonitor.cmdb.domain.model.identifier.CIId;
import io.onemonitor.cmdb.domain.model.identifier.CITypeId;
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
 * Repository interface for CI Aggregate.
 * 
 * This interface defines the contract for CI persistence operations.
 * Implementation should be in the infrastructure layer.
 */
public interface CIRepository {

    /**
     * Saves a CI aggregate.
     */
    CI save(CI ci);

    /**
     * Finds a CI by its ID.
     */
    Optional<CI> findById(CIId id);

    /**
     * Finds all CIs.
     */
    List<CI> findAll();

    /**
     * Finds CIs by their IDs.
     */
    List<CI> findByIdIn(Collection<CIId> ids);

    /**
     * Finds a CI by type and name.
     */
    Optional<CI> findByTypeIdAndName(CITypeId typeId, String name);

    /**
     * Checks if a CI exists with the given type and name.
     */
    boolean existsByTypeIdAndName(CITypeId typeId, String name);

    /**
     * Finds CIs by type ID.
     */
    List<CI> findByTypeId(CITypeId typeId);

    /**
     * Finds CIs by type ID with pagination.
     */
    Page<CI> findByTypeId(CITypeId typeId, Pageable pageable);

    /**
     * Finds CIs by status.
     */
    List<CI> findByStatus(CILifecycleStatus status);

    /**
     * Finds CIs by status with pagination.
     */
    Page<CI> findByStatus(CILifecycleStatus status, Pageable pageable);

    /**
     * Finds CIs by owner.
     */
    List<CI> findByOwner(String owner);

    /**
     * Finds CIs by environment.
     */
    List<CI> findByEnvironment(String environment);

    /**
     * Finds CIs by multiple statuses.
     */
    List<CI> findByStatusIn(Collection<CILifecycleStatus> statuses);

    /**
     * Searches CIs by name or display name.
     */
    List<CI> searchByName(String searchTerm);

    /**
     * Searches CIs by name or display name with pagination.
     */
    Page<CI> searchByName(String searchTerm, Pageable pageable);

    /**
     * Finds CIs created after a certain time.
     */
    List<CI> findByCreatedAtAfter(Instant timestamp);

    /**
     * Finds CIs updated after a certain time.
     */
    List<CI> findByUpdatedAtAfter(Instant timestamp);

    /**
     * Deletes a CI by ID.
     */
    void deleteById(CIId id);

    /**
     * Counts CIs by type ID.
     */
    long countByTypeId(CITypeId typeId);

    /**
     * Counts CIs by status.
     */
    long countByStatus(CILifecycleStatus status);

    /**
     * Finds CIs with custom query.
     */
    @Query("SELECT c FROM CI c WHERE " +
           "(:typeId IS NULL OR c.typeId = :typeId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:owner IS NULL OR c.owner = :owner) AND " +
           "(:environment IS NULL OR c.environment = :environment)")
    Page<CI> findByFilters(
        @Param("typeId") CITypeId typeId,
        @Param("status") CILifecycleStatus status,
        @Param("owner") String owner,
        @Param("environment") String environment,
        Pageable pageable
    );
}
