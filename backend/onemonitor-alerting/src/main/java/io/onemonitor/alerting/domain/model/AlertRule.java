package io.onemonitor.alerting.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.onemonitor.alerting.domain.model.identifier.AlertRuleId;
import io.onemonitor.common.domain.base.AggregateRoot;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.*;

/**
 * AlertRule Aggregate Root.
 * 
 * Represents an alert rule definition with PromQL expression,
 * severity, and notification configuration.
 */
@Entity
@Table(name = "alert_rules", indexes = {
    @Index(name = "idx_alert_rule_name", columnList = "name"),
    @Index(name = "idx_alert_rule_severity", columnList = "severity"),
    @Index(name = "idx_alert_rule_enabled", columnList = "enabled"),
    @Index(name = "idx_alert_rule_ci_id", columnList = "ci_id")
})
public class AlertRule extends AggregateRoot<AlertRuleId> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private AlertRuleId id;

    /**
     * Unique name for the alert rule.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    /**
     * Human-readable display name.
     */
    @Column(name = "display_name", length = 255)
    private String displayName;

    /**
     * Description of what this alert monitors.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Alert severity: CRITICAL, WARNING, INFO.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    /**
     * PromQL expression to evaluate.
     */
    @Column(name = "promql_expression", nullable = false, columnDefinition = "TEXT")
    private String promqlExpression;

    /**
     * Evaluation interval in seconds.
     */
    @Column(name = "evaluation_interval")
    private Integer evaluationInterval;

    /**
     * Duration for which the condition must be true before firing (seconds).
     */
    @Column(name = "for_duration")
    private Integer forDuration;

    /**
     * Summary template for alert notifications.
     */
    @Column(name = "summary_template", columnDefinition = "TEXT")
    private String summaryTemplate;

    /**
     * Description template for alert notifications.
     */
    @Column(name = "description_template", columnDefinition = "TEXT")
    private String descriptionTemplate;

    /**
     * Runbook URL or documentation link.
     */
    @Column(name = "runbook_url", length = 1000)
    private String runbookUrl;

    /**
     * Labels to add to firing alerts.
     */
    @JdbcTypeCode(JsonType.NAME)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> labels;

    /**
     * CI ID this alert is associated with (optional).
     */
    @Column(name = "ci_id", length = 36)
    private String ciId;

    /**
     * Whether this alert rule is enabled.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Whether to include in auto-resolve.
     */
    @Column(name = "auto_resolve")
    private boolean autoResolve = true;

    /**
     * Time after which resolved alerts are cleaned up (seconds).
     */
    @Column(name = "resolve_timeout")
    private Integer resolveTimeout;

    /**
     * Notification channels (JSON array of channel IDs).
     */
    @Column(name = "notification_channels", columnDefinition = "TEXT")
    private String notificationChannelsJson;

    /**
     * Number of times this rule has fired.
     */
    @Column(name = "fire_count")
    private long fireCount = 0;

    /**
     * Last time this rule fired.
     */
    @Column(name = "last_fired_at")
    private Instant lastFiredAt;

    /**
     * Created by user ID.
     */
    @Column(name = "created_by", length = 255)
    private String createdBy;

    protected AlertRule() {
        super();
    }

    /**
     * Factory method to create a new alert rule.
     */
    public static AlertRule create(String name, String promqlExpression, 
                                   AlertSeverity severity, String description) {
        AlertRule rule = new AlertRule();
        rule.id = AlertRuleId.generate();
        rule.name = validateName(name);
        rule.promqlExpression = validatePromQL(promqlExpression);
        rule.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        rule.description = description;
        rule.enabled = true;
        rule.evaluationInterval = 15; // default 15 seconds
        rule.forDuration = 0; // fire immediately
        rule.labels = new HashMap<>();
        rule.fireCount = 0;
        rule.createdAt = Instant.now();
        rule.updatedAt = rule.createdAt;

        return rule;
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Alert rule name cannot be null or blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Alert rule name cannot exceed 255 characters");
        }
        if (!name.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException(
                "Alert rule name must match [a-zA-Z0-9_-], got: " + name);
        }
        return name.toLowerCase().replace("_", "-");
    }

    private static String validatePromQL(String promql) {
        if (promql == null || promql.isBlank()) {
            throw new IllegalArgumentException("PromQL expression cannot be null or blank");
        }
        if (promql.length() > 10000) {
            throw new IllegalArgumentException("PromQL expression cannot exceed 10000 characters");
        }
        return promql;
    }

    /**
     * Updates the alert rule configuration.
     */
    public void update(String promqlExpression, AlertSeverity severity, String description,
                      Integer evaluationInterval, Integer forDuration, boolean enabled) {
        if (promqlExpression != null && !promqlExpression.isBlank()) {
            this.promqlExpression = validatePromQL(promqlExpression);
        }
        if (severity != null) {
            this.severity = severity;
        }
        if (description != null) {
            this.description = description;
        }
        if (evaluationInterval != null && evaluationInterval > 0) {
            this.evaluationInterval = evaluationInterval;
        }
        if (forDuration != null && forDuration >= 0) {
            this.forDuration = forDuration;
        }
        this.enabled = enabled;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates notification templates.
     */
    public void updateTemplates(String summaryTemplate, String descriptionTemplate,
                                String runbookUrl) {
        this.summaryTemplate = summaryTemplate;
        this.descriptionTemplate = descriptionTemplate;
        this.runbookUrl = runbookUrl;
        this.updatedAt = Instant.now();
    }

    /**
     * Adds a label to this alert rule.
     */
    public void addLabel(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Label key cannot be null or blank");
        }
        if (this.labels == null) {
            this.labels = new HashMap<>();
        }
        this.labels.put(key, value);
        this.updatedAt = Instant.now();
    }

    /**
     * Sets notification channels.
     */
    public void setNotificationChannels(List<String> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            this.notificationChannelsJson = null;
        } else {
            this.notificationChannelsJson = channelIds.toString();
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Enables this alert rule.
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Disables this alert rule.
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Records that this rule has fired.
     */
    public void recordFired() {
        this.fireCount++;
        this.lastFiredAt = Instant.now();
        this.updatedAt = this.lastFiredAt;
    }

    /**
     * Checks if this rule should be evaluated.
     */
    public boolean shouldEvaluate() {
        return this.enabled;
    }

    /**
     * Returns the full label set for matching.
     */
    public Map<String, String> getFullLabels() {
        Map<String, String> fullLabels = new HashMap<>();
        if (this.labels != null) {
            fullLabels.putAll(this.labels);
        }
        if (this.ciId != null) {
            fullLabels.put("ci_id", this.ciId);
        }
        return fullLabels;
    }

    /**
     * Interpolates the summary template with values.
     */
    public String interpolateSummary(Map<String, Object> values) {
        if (summaryTemplate == null || summaryTemplate.isBlank()) {
            return String.format("[%s] %s", severity.getDisplayName(), name);
        }
        return interpolateTemplate(summaryTemplate, values);
    }

    /**
     * Interpolates the description template with values.
     */
    public String interpolateDescription(Map<String, Object> values) {
        if (descriptionTemplate == null || descriptionTemplate.isBlank()) {
            return description != null ? description : "No description";
        }
        return interpolateTemplate(descriptionTemplate, values);
    }

    private String interpolateTemplate(String template, Map<String, Object> values) {
        String result = template;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", 
                                   String.valueOf(entry.getValue()));
        }
        return result;
    }

    // Getters
    @Override
    public AlertRuleId getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public String getPromqlExpression() {
        return promqlExpression;
    }

    public Integer getEvaluationInterval() {
        return evaluationInterval;
    }

    public Integer getForDuration() {
        return forDuration;
    }

    public String getSummaryTemplate() {
        return summaryTemplate;
    }

    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public String getRunbookUrl() {
        return runbookUrl;
    }

    public Map<String, String> getLabels() {
        return labels != null ? Collections.unmodifiableMap(new HashMap<>(labels)) : Collections.emptyMap();
    }

    public String getCiId() {
        return ciId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAutoResolve() {
        return autoResolve;
    }

    public Integer getResolveTimeout() {
        return resolveTimeout;
    }

    public String getNotificationChannelsJson() {
        return notificationChannelsJson;
    }

    public long getFireCount() {
        return fireCount;
    }

    public Instant getLastFiredAt() {
        return lastFiredAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    // Setters
    public void setId(AlertRuleId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public void setPromqlExpression(String promqlExpression) {
        this.promqlExpression = promqlExpression;
    }

    public void setEvaluationInterval(Integer evaluationInterval) {
        this.evaluationInterval = evaluationInterval;
    }

    public void setForDuration(Integer forDuration) {
        this.forDuration = forDuration;
    }

    public void setSummaryTemplate(String summaryTemplate) {
        this.summaryTemplate = summaryTemplate;
    }

    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public void setRunbookUrl(String runbookUrl) {
        this.runbookUrl = runbookUrl;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public void setCiId(String ciId) {
        this.ciId = ciId;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAutoResolve(boolean autoResolve) {
        this.autoResolve = autoResolve;
    }

    public void setResolveTimeout(Integer resolveTimeout) {
        this.resolveTimeout = resolveTimeout;
    }

    public void setNotificationChannelsJson(String notificationChannelsJson) {
        this.notificationChannelsJson = notificationChannelsJson;
    }

    public void setFireCount(long fireCount) {
        this.fireCount = fireCount;
    }

    public void setLastFiredAt(Instant lastFiredAt) {
        this.lastFiredAt = lastFiredAt;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Alert severity enumeration.
     */
    public enum AlertSeverity {
        CRITICAL("Critical", "严重", 1, "#F56C6C", "Immediate action required"),
        WARNING("Warning", "警告", 2, "#E6A23C", "Action required soon"),
        INFO("Info", "信息", 3, "#909399", "For informational purposes");

        private final String name;
        private final String chineseName;
        private final int priority;
        private final String color;
        private final String description;

        AlertSeverity(String name, String chineseName, int priority, 
                     String color, String description) {
            this.name = name;
            this.chineseName = chineseName;
            this.priority = priority;
            this.color = color;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getChineseName() {
            return chineseName;
        }

        public int getPriority() {
            return priority;
        }

        public String getColor() {
            return color;
        }

        public String getDescription() {
            return description;
        }

        public String getDisplayName() {
            return chineseName + " (" + name + ")";
        }
    }
}
