package io.onemonitor.monitoring.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.onemonitor.common.domain.base.AggregateRoot;
import io.onemonitor.monitoring.domain.model.identifier.MetricSeriesId;
import io.onemonitor.monitoring.domain.model.identifier.TargetId;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.*;

/**
 * MetricSeries Aggregate Root.
 * 
 * Represents a time-series metric definition.
 * Contains metric metadata but not the actual data points (stored in VictoriaMetrics).
 */
@Entity
@Table(name = "metric_series", indexes = {
    @Index(name = "idx_metric_name", columnList = "metric_name"),
    @Index(name = "idx_metric_target", columnList = "target_id"),
    @Index(name = "idx_metric_family", columnList = "metric_family")
})
public class MetricSeries extends AggregateRoot<MetricSeriesId> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private MetricSeriesId id;

    /**
     * The metric name following Prometheus naming conventions.
     * Must match: [a-zA-Z_][a-zA-Z0-9_]*
     */
    @Column(name = "metric_name", nullable = false, length = 255)
    private String metricName;

    /**
     * Human-readable display name.
     */
    @Column(name = "display_name", length = 255)
    private String displayName;

    /**
     * Metric family/type: COUNTER, GAUGE, HISTOGRAM, SUMMARY, INFO, STATESET
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "metric_family", nullable = false, length = 20)
    private MetricFamily family;

    /**
     * Description of the metric.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Unit of measurement (e.g., "seconds", "bytes", "percent").
     */
    @Column(length = 50)
    private String unit;

    /**
     * ID of the target that produces this metric.
     */
    @Column(name = "target_id", length = 36)
    private TargetId targetId;

    /**
     * Associated CI ID for correlation.
     */
    @Column(name = "ci_id", length = 36)
    private String ciId;

    /**
     * Static labels for this metric (applied to all data points).
     */
    @JdbcTypeCode(JsonType.NAME)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> staticLabels;

    /**
     * HELP text for the metric.
     */
    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;

    /**
     * Whether this metric is enabled for collection.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Whether this metric should be scraped.
     */
    @Column(name = "scrape_enabled", nullable = false)
    private boolean scrapeEnabled = true;

    /**
     * Data retention period in days.
     */
    @Column(name = "retention_days")
    private Integer retentionDays;

    /**
     * Downsample configuration (e.g., "5m", "1h").
     */
    @Column(length = 20)
    private String downsample;

    /**
     * Created by (user or discovery).
     */
    @Column(name = "created_by", length = 255)
    private String createdBy;

    protected MetricSeries() {
        super();
    }

    /**
     * Factory method to create a new metric series.
     */
    public static MetricSeries create(String metricName, MetricFamily family, 
                                      String description, TargetId targetId) {
        MetricSeries series = new MetricSeries();
        series.id = MetricSeriesId.generate();
        series.metricName = validateMetricName(metricName);
        series.family = Objects.requireNonNull(family, "Metric family cannot be null");
        series.description = description;
        series.targetId = targetId;
        series.staticLabels = new HashMap<>();
        series.enabled = true;
        series.scrapeEnabled = true;
        series.retentionDays = 90; // default 90 days
        series.createdAt = Instant.now();
        series.updatedAt = series.createdAt;

        return series;
    }

    private static String validateMetricName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be null or blank");
        }
        if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException(
                "Metric name must match [a-zA-Z_][a-zA-Z0-9_]*, got: " + name);
        }
        return name;
    }

    /**
     * Adds a static label to this metric series.
     */
    public void addStaticLabel(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Label key cannot be null or blank");
        }
        if (value == null) {
            throw new IllegalArgumentException("Label value for key '" + key + "' cannot be null");
        }
        if (this.staticLabels == null) {
            this.staticLabels = new HashMap<>();
        }
        this.staticLabels.put(key, value);
        this.updatedAt = Instant.now();
    }

    /**
     * Removes a static label.
     */
    public void removeStaticLabel(String key) {
        if (this.staticLabels != null) {
            this.staticLabels.remove(key);
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Updates metric metadata.
     */
    public void updateMetadata(String description, String unit, Integer retentionDays, 
                               String downsample, boolean enabled, boolean scrapeEnabled) {
        if (description != null) {
            this.description = description;
        }
        if (unit != null) {
            this.unit = unit;
        }
        if (retentionDays != null && retentionDays > 0) {
            this.retentionDays = retentionDays;
        }
        if (downsample != null) {
            this.downsample = downsample;
        }
        this.enabled = enabled;
        this.scrapeEnabled = scrapeEnabled;
        this.updatedAt = Instant.now();
    }

    /**
     * Disables this metric series.
     */
    public void disable() {
        this.enabled = false;
        this.scrapeEnabled = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Enables this metric series.
     */
    public void enable() {
        this.enabled = true;
        this.scrapeEnabled = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Associates this metric with a CI.
     */
    public void associateWithCI(String ciId) {
        this.ciId = ciId;
        this.updatedAt = Instant.now();
    }

    /**
     * Returns the full label set for queries.
     */
    public Map<String, String> getFullLabels() {
        Map<String, String> labels = new HashMap<>();
        if (this.staticLabels != null) {
            labels.putAll(this.staticLabels);
        }
        if (this.targetId != null) {
            labels.put("target_id", this.targetId.getValue());
        }
        if (this.ciId != null) {
            labels.put("ci_id", this.ciId);
        }
        return labels;
    }

    /**
     * Returns the PromQL expression for this metric.
     */
    public String toPromQL() {
        StringBuilder sb = new StringBuilder();
        sb.append(metricName);
        
        if (staticLabels != null && !staticLabels.isEmpty()) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, String> entry : staticLabels.entrySet()) {
                if (!first) sb.append(",");
                sb.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
                first = false;
            }
            sb.append("}");
        }
        
        return sb.toString();
    }

    // Getters
    @Override
    public MetricSeriesId getId() {
        return this.id;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MetricFamily getFamily() {
        return family;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public TargetId getTargetId() {
        return targetId;
    }

    public String getCiId() {
        return ciId;
    }

    public Map<String, String> getStaticLabels() {
        return staticLabels != null ? Collections.unmodifiableMap(new HashMap<>(staticLabels)) : Collections.emptyMap();
    }

    public String getHelpText() {
        return helpText;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isScrapeEnabled() {
        return scrapeEnabled;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public String getDownsample() {
        return downsample;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    // Setters
    public void setId(MetricSeriesId id) {
        this.id = id;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setFamily(MetricFamily family) {
        this.family = family;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setTargetId(TargetId targetId) {
        this.targetId = targetId;
    }

    public void setCiId(String ciId) {
        this.ciId = ciId;
    }

    public void setStaticLabels(Map<String, String> staticLabels) {
        this.staticLabels = staticLabels;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setScrapeEnabled(boolean scrapeEnabled) {
        this.scrapeEnabled = scrapeEnabled;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public void setDownsample(String downsample) {
        this.downsample = downsample;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Metric family/type enumeration.
     */
    public enum MetricFamily {
        COUNTER("counter", "Cumulative counter that only increases"),
        GAUGE("gauge", "Value that can go up and down"),
        HISTOGRAM("histogram", "Samples observations in buckets"),
        SUMMARY("summary", "Quantiles over a sliding time window"),
        INFO("info", "Informational metadata"),
        STATESET("stateset", "State of a feature or component");

        private final String promType;
        private final String description;

        MetricFamily(String promType, String description) {
            this.promType = promType;
            this.description = description;
        }

        public String getPromType() {
            return promType;
        }

        public String getDescription() {
            return description;
        }
    }
}
