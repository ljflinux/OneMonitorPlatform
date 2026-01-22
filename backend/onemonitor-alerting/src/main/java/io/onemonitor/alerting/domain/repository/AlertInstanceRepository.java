package io.onemonitor.alerting.domain.repository;

import io.onemonitor.alerting.domain.model.AlertInstance;
import io.onemonitor.alerting.domain.model.AlertInstance.AlertStatus;
import io.onemonitor.alerting.domain.model.AlertRule.AlertSeverity;
import io.onemonitor.alerting.domain.model.identifier.AlertInstanceId;
import io.onemonitor.alerting.domain.model.identifier.AlertRuleId;
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
 * Repository interface for AlertInstance Aggregate.
 */
public interface AlertInstanceRepository {

    /**
     * Saves an alert instance.
     */
    AlertInstance save(AlertInstance instance);

    /**
     * Finds an alert instance by ID.
     */
    Optional<AlertInstance> findById(AlertInstanceId id);

    /**
     * Finds an alert instance by fingerprint.
     */
    Optional<AlertInstance> findByFingerprint(String fingerprint);

    /**
     * Finds all alert instances.
     */
    List<AlertInstance> findAll();

    /**
     * Finds alert instances by IDs.
     */
    List<AlertInstance> findByIdIn(Collection<AlertInstanceId> ids);

    /**
     * Finds alert instances by rule ID.
     */
    List<AlertInstance> findByRuleId(AlertRuleId ruleId);

    /**
     * Finds alert instances by status.
     */
    List<AlertInstance> findByStatus(AlertStatus status);

    /**
     * Finds alert instances by status with pagination.
     */
    Page<AlertInstance> findByStatus(AlertStatus status, Pageable pageable);

    /**
     * Finds alert instances by severity.
     */
    List<AlertInstance> findBySeverity(AlertSeverity severity);

    /**
     * Finds firing alerts by severity with pagination.
     */
    Page<AlertInstance> findBySeverityAndStatus(
        AlertSeverity severity, AlertStatus status, Pageable pageable);

    /**
     * Finds alert instances for a CI.
     */
    List<AlertInstance> findByCiId(String ciId);

    /**
     * Finds alert instances by CI and status.
     */
    List<AlertInstance> findByCiIdAndStatus(String ciId, AlertStatus status);

    /**
     * Finds firing alerts ordered by severity and time.
     */
    @Query("SELECT a FROM AlertInstance a WHERE a.status = :status " +
           "ORDER BY a.severity ASC, a.firedAt DESC")
    List<AlertInstance> findFiringAlertsOrderBySeverity(
        @Param("status") AlertStatus status);

    /**
     * Counts alerts by status.
     */
    long countByStatus(AlertStatus status);

    /**
     * Counts alerts by severity and status.
     */
    long countBySeverityAndStatus(AlertSeverity severity, AlertStatus status);

    /**
     * Counts alerts for a CI by status.
     */
    long countByCiIdAndStatus(String ciId, AlertStatus status);

    /**
     * Finds recent alerts.
     */
    @Query("SELECT a FROM AlertInstance a WHERE a.firedAt >= :since ORDER BY a.firedAt DESC")
    List<AlertInstance> findRecentAlerts(@Param("since") Instant since, Pageable pageable);

    /**
     * Finds alerts that need auto-resolution check.
     */
    @Query("SELECT a FROM AlertInstance a WHERE " +
           "a.status = :status AND " +
           "a.autoResolved = true AND " +
           "a.resolvedAt IS NULL AND " +
           "a.currentValue IS NULL")
    List<AlertInstance> findAlertsForAutoResolveCheck(@Param("status") AlertStatus status);

    /**
     * Finds alerts for a time range.
     */
    @Query("SELECT a FROM AlertInstance a WHERE " +
           "a.firedAt BETWEEN :start AND :end " +
           "ORDER BY a.firedAt DESC")
    Page<AlertInstance> findAlertsInTimeRange(
        @Param("start") Instant start,
        @Param("end") Instant end,
        Pageable pageable
    );

    /**
     * Deletes resolved alerts older than a threshold.
     */
    @Query("DELETE FROM AlertInstance a WHERE " +
           "a.status = :status AND " +
           "a.resolvedAt < :threshold")
    void deleteResolvedAlertsOlderThan(
        @Param("status") AlertStatus status,
        @Param("threshold") Instant threshold
    );

    /**
     * Deletes an alert instance by ID.
     */
    void deleteById(AlertInstanceId id);

    /**
     * Finds alert instances with custom query.
     */
    @Query("SELECT a FROM AlertInstance a WHERE " +
           "(:ruleId IS NULL OR a.ruleId = :ruleId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:severity IS NULL OR a.severity = :severity) AND " +
           "(:ciId IS NULL OR a.ciId = :ciId)")
    Page<AlertInstance> findByFilters(
        @Param("ruleId") AlertRuleId ruleId,
        @Param("status") AlertStatus status,
        @Param("severity") AlertSeverity severity,
        @Param("ciId") String ciId,
        Pageable pageable
    );
}
