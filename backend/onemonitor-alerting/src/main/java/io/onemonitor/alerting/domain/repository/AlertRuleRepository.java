package io.onemonitor.alerting.domain.repository;

import io.onemonitor.alerting.domain.model.AlertRule;
import io.onemonitor.alerting.domain.model.AlertRule.AlertSeverity;
import io.onemonitor.alerting.domain.model.identifier.AlertRuleId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AlertRule Aggregate.
 */
public interface AlertRuleRepository {

    /**
     * Saves an alert rule.
     */
    AlertRule save(AlertRule rule);

    /**
     * Finds an alert rule by ID.
     */
    Optional<AlertRule> findById(AlertRuleId id);

    /**
     * Finds all alert rules.
     */
    List<AlertRule> findAll();

    /**
     * Finds alert rules by IDs.
     */
    List<AlertRule> findByIdIn(Collection<AlertRuleId> ids);

    /**
     * Finds an alert rule by name.
     */
    Optional<AlertRule> findByName(String name);

    /**
     * Checks if an alert rule exists with the given name.
     */
    boolean existsByName(String name);

    /**
     * Finds enabled alert rules.
     */
    List<AlertRule> findByEnabledTrue();

    /**
     * Finds enabled alert rules with pagination.
     */
    Page<AlertRule> findByEnabledTrue(Pageable pageable);

    /**
     * Finds alert rules by severity.
     */
    List<AlertRule> findBySeverity(AlertSeverity severity);

    /**
     * Finds alert rules by severity with pagination.
     */
    Page<AlertRule> findBySeverity(AlertSeverity severity, Pageable pageable);

    /**
     * Finds alert rules associated with a CI.
     */
    List<AlertRule> findByCiId(String ciId);

    /**
     * Counts alert rules by severity.
     */
    long countBySeverity(AlertSeverity severity);

    /**
     * Counts enabled alert rules.
     */
    long countByEnabledTrue();

    /**
     * Finds alert rules that need evaluation.
     */
    @Query("SELECT r FROM AlertRule r WHERE r.enabled = true AND " +
           "(r.lastFiredAt IS NULL OR " +
           "r.lastFiredAt <= :now - INTERVAL '1 second' * r.evaluationInterval)")
    List<AlertRule> findRulesNeedingEvaluation(@Param("now") java.time.Instant now);

    /**
     * Deletes an alert rule by ID.
     */
    void deleteById(AlertRuleId id);

    /**
     * Finds alert rules with custom query.
     */
    @Query("SELECT r FROM AlertRule r WHERE " +
           "(:severity IS NULL OR r.severity = :severity) AND " +
           "(:enabled IS NULL OR r.enabled = :enabled) AND " +
           "(:ciId IS NULL OR r.ciId = :ciId)")
    Page<AlertRule> findByFilters(
        @Param("severity") AlertSeverity severity,
        @Param("enabled") Boolean enabled,
        @Param("ciId") String ciId,
        Pageable pageable
    );

    /**
     * Searches alert rules by name or description.
     */
    @Query("SELECT r FROM AlertRule r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<AlertRule> searchByNameOrDescription(
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
}
