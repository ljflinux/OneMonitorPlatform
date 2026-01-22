package io.onemonitor.monitoring.application.dto;

import io.onemonitor.common.domain.vo.LabelSet;
import io.onemonitor.monitoring.domain.model.Target.TargetType;

import java.time.Instant;

/**
 * Command to create a new Target.
 */
public record TargetCreateCommand(
    String name,
    TargetType type,
    String endpoint,
    Integer port,
    String ciId,
    LabelSet labels,
    String description,
    Integer scrapeInterval,
    Integer scrapeTimeout,
    String createdBy
) {}

/**
 * Command to update a Target.
 */
public record TargetUpdateCommand(
    String name,
    String endpoint,
    Integer port,
    Integer scrapeInterval,
    Integer scrapeTimeout,
    Boolean enabled,
    String description
) {}

/**
 * Response DTO for Target data.
 */
public record TargetResponse(
    String id,
    String name,
    String description,
    String type,
    String typeDisplayName,
    String endpoint,
    Integer port,
    String status,
    String statusDisplayName,
    String ciId,
    String labelsJson,
    Instant lastScrapeSuccess,
    Instant lastScrapeFailure,
    String lastScrapeError,
    Instant nextScrape,
    Integer scrapeInterval,
    Integer scrapeTimeout,
    boolean enabled,
    Instant createdAt,
    Instant updatedAt
) {}

/**
 * Query parameters for Target search.
 */
public record TargetQuery(
    TargetType type,
    String status,
    String ciId,
    Boolean enabled,
    String searchTerm,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {}
