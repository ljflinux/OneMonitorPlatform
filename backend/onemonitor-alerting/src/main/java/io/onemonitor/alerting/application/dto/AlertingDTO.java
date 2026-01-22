package io.onemonitor.alerting.application.dto;

import io.onemonitor.alerting.domain.model.AlertRule.AlertSeverity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Command to create a new Alert Rule.
 */
public record AlertRuleCreateCommand(
    String name,
    String displayName,
    String description,
    AlertSeverity severity,
    String promqlExpression,
    Integer evaluationInterval,
    Integer forDuration,
    String summaryTemplate,
    String descriptionTemplate,
    String runbookUrl,
    Map<String, String> labels,
    String ciId,
    List<String> notificationChannelIds,
    Boolean autoResolve,
    Integer resolveTimeout,
    String createdBy
) {}

/**
 * Command to update an Alert Rule.
 */
public record AlertRuleUpdateCommand(
    String description,
    AlertSeverity severity,
    String promqlExpression,
    Integer evaluationInterval,
    Integer forDuration,
    String summaryTemplate,
    String descriptionTemplate,
    String runbookUrl,
    Map<String, String> labels,
    List<String> notificationChannelIds,
    Boolean autoResolve,
    Integer resolveTimeout,
    Boolean enabled
) {}

/**
 * Response DTO for Alert Rule data.
 */
public record AlertRuleResponse(
    String id,
    String name,
    String displayName,
    String description,
    String severity,
    String severityDisplayName,
    String promqlExpression,
    Integer evaluationInterval,
    Integer forDuration,
    String summaryTemplate,
    String descriptionTemplate,
    String runbookUrl,
    Map<String, String> labels,
    String ciId,
    List<String> notificationChannelIds,
    boolean enabled,
    boolean autoResolve,
    Integer resolveTimeout,
    long fireCount,
    Instant lastFiredAt,
    Instant createdAt,
    Instant updatedAt,
    String createdBy
) {}

/**
 * Command to create a new Alert Instance (internal use).
 */
public record AlertFireCommand(
    String ruleId,
    String fingerprint,
    Map<String, String> labels,
    Map<String, Object> values,
    double currentValue
) {}

/**
 * Command to acknowledge an alert.
 */
public record AlertAckCommand(
    String userId,
    String comment
) {}

/**
 * Command to resolve an alert.
 */
public record AlertResolveCommand(
    String userId,
    String comment,
    boolean autoResolved
) {}

/**
 * Query parameters for Alert Rule search.
 */
public record AlertRuleQuery(
    AlertSeverity severity,
    Boolean enabled,
    String ciId,
    String searchTerm,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {}

/**
 * Query parameters for Alert Instance search.
 */
public record AlertInstanceQuery(
    String ruleId,
    String status,
    String severity,
    String ciId,
    Instant startTime,
    Instant endTime,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {}

/**
 * Response DTO for Alert Instance data.
 */
public record AlertInstanceResponse(
    String id,
    String ruleId,
    String ruleName,
    String fingerprint,
    String status,
    String statusDisplayName,
    String severity,
    String severityDisplayName,
    String ciId,
    Map<String, String> labels,
    Map<String, Object> values,
    String currentValue,
    String summary,
    String description,
    String runbookUrl,
    int fireCount,
    Instant firedAt,
    Instant updatedAt,
    Instant acknowledgedAt,
    String acknowledgedBy,
    String ackComment,
    Instant resolvedAt,
    String resolvedBy,
    String resolveComment,
    boolean autoResolved,
    int notificationCount,
    String duration
) {}

/**
 * Response DTO for Alert statistics.
 */
public record AlertStatsResponse(
    long totalActive,
    long totalFiring,
    long totalAcknowledged,
    long totalResolved,
    long criticalCount,
    long warningCount,
    long infoCount,
    long last24HoursFired,
    long last24HoursResolved
) {}
