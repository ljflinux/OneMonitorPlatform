package io.onemonitor.monitoring.domain.model;

import io.onemonitor.common.domain.base.AggregateRoot;
import io.onemonitor.common.domain.vo.LabelSet;
import io.onemonitor.monitoring.domain.model.identifier.TargetId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

/**
 * Monitoring Target Aggregate Root.
 * 
 * Represents a target system/service to be monitored.
 * Examples: server, application, database, Kubernetes cluster, cloud service.
 */
@Entity
@Table(name = "monitoring_targets", indexes = {
    @Index(name = "idx_target_type", columnList = "target_type"),
    @Index(name = "idx_target_status", columnList = "status"),
    @Index(name = "idx_target_ci_id", columnList = "ci_id"),
    @Index(name = "idx_target_endpoint", columnList = "endpoint")
})
public class Target extends AggregateRoot<TargetId> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private TargetId id;

    /**
     * Human-readable name for the target.
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Description of the target.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Type of target: SERVER, DATABASE, APPLICATION, KUBERNETES, CLOUD, NETWORK, CUSTOM
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private TargetType type;

    /**
     * Monitoring endpoint URL or address.
     */
    @Column(nullable = false, length = 500)
    private String endpoint;

    /**
     * Port for the monitoring endpoint.
     */
    @Column
    private Integer port;

    /**
     * Current health status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TargetStatus status;

    /**
     * Associated CI ID from CMDB for correlation.
     */
    @Column(name = "ci_id", length = 36)
    private String ciId;

    /**
     * Monitoring labels for metrics/logs correlation.
     */
    @Column(columnDefinition = "TEXT")
    private String labelsJson;

    /**
     * Collection configuration (scrape interval, timeout, etc.)
     */
    @Column(columnDefinition = "TEXT")
    private String collectionConfig;

    /**
     * HTTP/SNMPAuthentication settings.
     */
    @Column(columnDefinition = "TEXT")
    private String authConfig;

    /**
     * Last successful scrape time.
     */
    @Column(name = "last_scrape_success")
    private Instant lastScrapeSuccess;

    /**
     * Last failed scrape time.
     */
    @Column(name = "last_scrape_failure")
    private Instant lastScrapeFailure;

    /**
     * Last scrape error message.
     */
    @Column(name = "last_scrape_error", columnDefinition = "TEXT")
    private String lastScrapeError;

    /**
     * Next scheduled scrape time.
     */
    @Column(name = "next_scrape")
    private Instant nextScrape;

    /**
     * Scrape interval in seconds.
     */
    @Column(name = "scrape_interval")
    private Integer scrapeInterval;

    /**
     * Timeout for scrape in seconds.
     */
    @Column(name = "scrape_timeout")
    private Integer scrapeTimeout;

    /**
     * Whether target is enabled for monitoring.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    protected Target() {
        super();
    }

    /**
     * Factory method to create a new monitoring target.
     */
    public static Target create(String name, TargetType type, String endpoint, 
                                Integer port, String ciId, LabelSet labels) {
        Target target = new Target();
        target.id = TargetId.generate();
        target.name = validateName(name);
        target.type = Objects.requireNonNull(type, "Target type cannot be null");
        target.endpoint = validateEndpoint(endpoint);
        target.port = port;
        target.ciId = ciId;
        target.status = TargetStatus.UNKNOWN;
        target.scrapeInterval = 15; // default 15 seconds
        target.scrapeTimeout = 10;  // default 10 seconds
        target.enabled = true;
        target.createdAt = Instant.now();
        target.updatedAt = target.createdAt;

        return target;
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Target name cannot be null or blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Target name cannot exceed 255 characters");
        }
        return name;
    }

    private static String validateEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Endpoint cannot be null or blank");
        }
        if (endpoint.length() > 500) {
            throw new IllegalArgumentException("Endpoint cannot exceed 500 characters");
        }
        return endpoint;
    }

    /**
     * Records a successful scrape.
     */
    public void recordScrapeSuccess() {
        this.lastScrapeSuccess = Instant.now();
        this.lastScrapeFailure = null;
        this.lastScrapeError = null;
        this.status = TargetStatus.HEALTHY;
        this.updatedAt = Instant.now();
    }

    /**
     * Records a failed scrape.
     */
    public void recordScrapeFailure(String errorMessage) {
        this.lastScrapeFailure = Instant.now();
        this.lastScrapeError = errorMessage;
        
        // Update status based on consecutive failures
        if (this.status == TargetStatus.UNHEALTHY) {
            // Already unhealthy, stay unhealthy
        } else if (this.status == TargetStatus.DEGRADED) {
            this.status = TargetStatus.UNHEALTHY;
        } else {
            this.status = TargetStatus.DEGRADED;
        }
        
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the target configuration.
     */
    public void updateConfig(String name, String endpoint, Integer port,
                            Integer scrapeInterval, Integer scrapeTimeout, boolean enabled) {
        if (name != null && !name.isBlank()) {
            this.name = validateName(name);
        }
        if (endpoint != null && !endpoint.isBlank()) {
            this.endpoint = validateEndpoint(endpoint);
        }
        this.port = port;
        if (scrapeInterval != null && scrapeInterval > 0) {
            this.scrapeInterval = scrapeInterval;
        }
        if (scrapeTimeout != null && scrapeTimeout > 0) {
            this.scrapeTimeout = scrapeTimeout;
        }
        this.enabled = enabled;
        this.updatedAt = Instant.now();
    }

    /**
     * Associates this target with a CI.
     */
    public void associateWithCI(String ciId) {
        this.ciId = ciId;
        this.updatedAt = Instant.now();
    }

    /**
     * Disassociates this target from CI.
     */
    public void disassociateFromCI() {
        this.ciId = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the target is healthy.
     */
    public boolean isHealthy() {
        return this.status == TargetStatus.HEALTHY;
    }

    /**
     * Checks if the target needs to be scraped.
     */
    public boolean needsScrape() {
        if (!enabled || status == TargetStatus.DISABLED) {
            return false;
        }
        if (nextScrape == null) {
            return true;
        }
        return Instant.now().isAfter(nextScrape);
    }

    // Getters
    @Override
    public TargetId getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TargetType getType() {
        return type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Integer getPort() {
        return port;
    }

    public TargetStatus getStatus() {
        return status;
    }

    public String getCiId() {
        return ciId;
    }

    public String getLabelsJson() {
        return labelsJson;
    }

    public Instant getLastScrapeSuccess() {
        return lastScrapeSuccess;
    }

    public Instant getLastScrapeFailure() {
        return lastScrapeFailure;
    }

    public String getLastScrapeError() {
        return lastScrapeError;
    }

    public Instant getNextScrape() {
        return nextScrape;
    }

    public Integer getScrapeInterval() {
        return scrapeInterval;
    }

    public Integer getScrapeTimeout() {
        return scrapeTimeout;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Setters
    public void setId(TargetId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(TargetType type) {
        this.type = type;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setStatus(TargetStatus status) {
        this.status = status;
    }

    public void setCiId(String ciId) {
        this.ciId = ciId;
    }

    public void setLabelsJson(String labelsJson) {
        this.labelsJson = labelsJson;
    }

    public void setLastScrapeSuccess(Instant lastScrapeSuccess) {
        this.lastScrapeSuccess = lastScrapeSuccess;
    }

    public void setLastScrapeFailure(Instant lastScrapeFailure) {
        this.lastScrapeFailure = lastScrapeFailure;
    }

    public void setLastScrapeError(String lastScrapeError) {
        this.lastScrapeError = lastScrapeError;
    }

    public void setNextScrape(Instant nextScrape) {
        this.nextScrape = nextScrape;
    }

    public void setScrapeInterval(Integer scrapeInterval) {
        this.scrapeInterval = scrapeInterval;
    }

    public void setScrapeTimeout(Integer scrapeTimeout) {
        this.scrapeTimeout = scrapeTimeout;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Target type enumeration.
     */
    public enum TargetType {
        SERVER("服务器", "Physical or virtual server"),
        DATABASE("数据库", "Database server"),
        APPLICATION("应用程序", "Application service"),
        KUBERNETES("Kubernetes", "Kubernetes cluster"),
        CLOUD("云服务", "Cloud service (AWS, Azure, GCP)"),
        NETWORK("网络设备", "Network device (router, switch, firewall)"),
        CONTAINER("容器", "Container instance"),
        MIDDLEWARE("中间件", "Middleware (消息队列, 缓存)"),
        CUSTOM("自定义", "Custom endpoint");

        private final String displayName;
        private final String description;

        TargetType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Target status enumeration.
     */
    public enum TargetStatus {
        HEALTHY("健康", "Target is responding normally"),
        DEGRADED("降级", "Target is responding with errors"),
        UNHEALTHY("不健康", "Target is not responding"),
        UNKNOWN("未知", "Target status is unknown"),
        DISABLED("已禁用", "Target monitoring is disabled");

        private final String displayName;
        private final String description;

        TargetStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }
}
