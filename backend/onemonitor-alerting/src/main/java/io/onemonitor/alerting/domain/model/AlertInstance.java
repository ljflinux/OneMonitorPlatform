package io.onemonitor.alerting.domain.model;

import io.onemonitor.alerting.domain.model.identifier.AlertInstanceId;
import io.onemonitor.alerting.domain.model.identifier.AlertRuleId;
import io.onemonitor.common.domain.base.AggregateRoot;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

/**
 * AlertInstance Aggregate Root.
 * 
 * Represents an actual firing alert with state machine.
 * State transitions: FIRING -> ACKNOWLEDGED -> RESOLVED
 */
@Entity
@Table(name = "alert_instances", indexes = {
    @Index(name = "idx_alert_instance_rule_id", columnList = "rule_id"),
    @Index(name = "idx_alert_instance_status", columnList = "status"),
    @Index(name = "idx_alert_instance_fired_at", columnList = "fired_at"),
    @Index(name = "idx_alert_instance_ci_id", columnList = "ci_id"),
    @Index(name = "idx_alert_instance_fingerprint", columnList = "fingerprint")
})
public class AlertInstance extends AggregateRoot<AlertInstanceId> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private AlertInstanceId id;

    /**
     * ID of the alert rule that generated this alert.
     */
    @Column(name = "rule_id", nullable = false, length = 36)
    private AlertRuleId ruleId;

    /**
     * Name of the alert rule (denormalized for queries).
     */
    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    /**
     * Fingerprint for deduplication (hash of labels).
     */
    @Column(nullable = false, length = 64)
    private String fingerprint;

    /**
     * Current status of the alert.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    /**
     * Severity from the rule (denormalized).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertRule.AlertSeverity severity;

    /**
     * CI ID this alert is associated with.
     */
    @Column(name = "ci_id", length = 36)
    private String ciId;

    /**
     * Labels from the evaluation (JSON).
     */
    @Column(columnDefinition = "TEXT")
    private String labelsJson;

    /**
     * Values at the time of firing (JSON).
     */
    @Column(columnDefinition = "TEXT")
    private String valuesJson;

    /**
     * Current value if still firing.
     */
    @Column(columnDefinition = "TEXT")
    private String currentValue;

    /**
     * Summary at the time of firing.
     */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /**
     * Description at the time of firing.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Runbook URL.
     */
    @Column(name = "runbook_url", length = 1000)
    private String runbookUrl;

    /**
     * Number of times this alert has fired (for flapping detection).
     */
    @Column(name = "fire_count")
    private int fireCount = 1;

    /**
     * When the alert first fired.
     */
    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    /**
     * When the alert was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * When the alert was acknowledged.
     */
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    /**
     * Who acknowledged the alert.
     */
    @Column(name = "acknowledged_by", length = 255)
    private String acknowledgedBy;

    /**
     * Acknowledgment comment.
     */
    @Column(name = "ack_comment", columnDefinition = "TEXT")
    private String ackComment;

    /**
     * When the alert was resolved.
     */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /**
     * Who resolved the alert.
     */
    @Column(name = "resolved_by", length = 255)
    private String resolvedBy;

    /**
     * Resolution comment.
     */
    @Column(name = "resolve_comment", columnDefinition = "TEXT")
    private String resolveComment;

    /**
     * Whether auto-resolved.
     */
    @Column(name = "auto_resolved")
    private boolean autoResolved = false;

    /**
     * Number of notifications sent.
     */
    @Column(name = "notification_count")
    private int notificationCount = 0;

    protected AlertInstance() {
        super();
    }

    /**
     * Factory method to create a new alert instance from a rule firing.
     */
    public static AlertInstance fire(AlertRule rule, String fingerprint, 
                                    Map<String, String> labels,
                                    Map<String, Object> values,
                                    double currentValue) {
        AlertInstance instance = new AlertInstance();
        instance.id = AlertInstanceId.generate();
        instance.ruleId = rule.getId();
        instance.ruleName = rule.getName();
        instance.fingerprint = validateFingerprint(fingerprint);
        instance.status = AlertStatus.FIRING;
        instance.severity = rule.getSeverity();
        instance.labelsJson = labels.toString();
        instance.valuesJson = values.toString();
        instance.currentValue = String.valueOf(currentValue);
        instance.summary = rule.interpolateSummary(values);
        instance.description = rule.interpolateDescription(values);
        instance.runbookUrl = rule.getRunbookUrl();
        instance.fireCount = 1;
        instance.firedAt = Instant.now();
        instance.updatedAt = instance.firedAt;

        return instance;
    }

    private static String validateFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            throw new IllegalArgumentException("Fingerprint cannot be null or blank");
        }
        if (fingerprint.length() > 64) {
            throw new IllegalArgumentException("Fingerprint cannot exceed 64 characters");
        }
        return fingerprint;
    }

    /**
     * Acknowledges this alert.
     */
    public void acknowledge(String userId, String comment) {
        if (this.status != AlertStatus.FIRING) {
            throw new IllegalStateException(
                "Cannot acknowledge alert in status " + this.status);
        }
        
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = Instant.now();
        this.acknowledgedBy = userId;
        this.ackComment = comment;
        this.updatedAt = this.acknowledgedAt;
    }

    /**
     * Resolves this alert.
     */
    public void resolve(String userId, String comment, boolean autoResolved) {
        if (this.status == AlertStatus.RESOLVED) {
            throw new IllegalStateException("Alert is already resolved");
        }
        
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = Instant.now();
        this.resolvedBy = userId;
        this.resolveComment = comment;
        this.autoResolved = autoResolved;
        this.updatedAt = this.resolvedAt;
        this.currentValue = null;
    }

    /**
     * Re-fires the alert (was resolved, now firing again).
     */
    public void refire() {
        if (this.status != AlertStatus.RESOLVED) {
            throw new IllegalStateException(
                "Cannot refire alert in status " + this.status);
        }
        
        this.status = AlertStatus.FIRING;
        this.fireCount++;
        this.resolvedAt = null;
        this.resolvedBy = null;
        this.resolveComment = null;
        this.autoResolved = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the alert can be acknowledged.
     */
    public boolean canAcknowledge() {
        return this.status == AlertStatus.FIRING;
    }

    /**
     * Checks if the alert can be resolved.
     */
    public boolean canResolve() {
        return this.status == AlertStatus.FIRING || this.status == AlertStatus.ACKNOWLEDGED;
    }

    /**
     * Increments the notification count.
     */
    public void incrementNotificationCount() {
        this.notificationCount++;
    }

    /**
     * Returns time elapsed since firing.
     */
    public long getElapsedSeconds() {
        Instant start = this.status == AlertStatus.RESOLVED ? 
                       this.resolvedAt : Instant.now();
        return start.getEpochSecond() - this.firedAt.getEpochSecond();
    }

    /**
     * Returns human-readable duration.
     */
    public String getDuration() {
        long seconds = getElapsedSeconds();
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "h";
        } else {
            return (seconds / 86400) + "d";
        }
    }

    // Getters
    @Override
    public AlertInstanceId getId() {
        return this.id;
    }

    public AlertRuleId getRuleId() {
        return ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public AlertRule.AlertSeverity getSeverity() {
        return severity;
    }

    public String getCiId() {
        return ciId;
    }

    public String getLabelsJson() {
        return labelsJson;
    }

    public String getValuesJson() {
        return valuesJson;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getRunbookUrl() {
        return runbookUrl;
    }

    public int getFireCount() {
        return fireCount;
    }

    public Instant getFiredAt() {
        return firedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public String getAckComment() {
        return ackComment;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public String getResolveComment() {
        return resolveComment;
    }

    public boolean isAutoResolved() {
        return autoResolved;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    // Setters
    public void setId(AlertInstanceId id) {
        this.id = id;
    }

    public void setRuleId(AlertRuleId ruleId) {
        this.ruleId = ruleId;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public void setSeverity(AlertRule.AlertSeverity severity) {
        this.severity = severity;
    }

    public void setCiId(String ciId) {
        this.ciId = ciId;
    }

    public void setLabelsJson(String labelsJson) {
        this.labelsJson = labelsJson;
    }

    public void setValuesJson(String valuesJson) {
        this.valuesJson = valuesJson;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRunbookUrl(String runbookUrl) {
        this.runbookUrl = runbookUrl;
    }

    public void setFireCount(int fireCount) {
        this.fireCount = fireCount;
    }

    public void setFiredAt(Instant firedAt) {
        this.firedAt = firedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public void setAckComment(String ackComment) {
        this.ackComment = ackComment;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public void setResolveComment(String resolveComment) {
        this.resolveComment = resolveComment;
    }

    public void setAutoResolved(boolean autoResolved) {
        this.autoResolved = autoResolved;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    /**
     * Alert status enumeration.
     */
    public enum AlertStatus {
        FIRING("Firing", " firing", "Alert is active and firing"),
        ACKNOWLEDGED("Acknowledged", " acknowledged", "Alert has been acknowledged"),
        RESOLVED("Resolved", " resolved", "Alert has been resolved");

        private final String name;
        private final String suffix;
        private final String description;

        AlertStatus(String name, String suffix, String description) {
            this.name = name;
            this.suffix = suffix;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getDescription() {
            return description;
        }
    }
}
