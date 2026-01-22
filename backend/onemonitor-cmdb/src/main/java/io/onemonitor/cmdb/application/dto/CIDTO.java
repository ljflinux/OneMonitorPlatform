package io.onemonitor.cmdb.application.dto;

import io.onemonitor.cmdb.domain.model.CILifecycleStatus;
import io.onemonitor.cmdb.domain.model.identifier.CITypeId;
import io.onemonitor.common.domain.vo.LabelSet;

import java.time.Instant;
import java.util.Map;

/**
 * Command to create a new CI.
 */
public record CICreateCommand(
    CITypeId typeId,
    String name,
    String displayName,
    Map<String, Object> attributes,
    LabelSet labels,
    String owner,
    String department,
    String location,
    String environment,
    String description,
    String createdBy
) {}

/**
 * Command to update an existing CI.
 */
public record CIUpdateCommand(
    Map<String, Object> attributes,
    LabelSet labels,
    String owner,
    String displayName,
    String department,
    String location,
    String environment,
    String description,
    String updatedBy
) {}

/**
 * Response DTO for CI data.
 */
public record CIResponse(
    String id,
    String typeId,
    String name,
    String displayName,
    String status,
    String statusDisplayName,
    Map<String, Object> attributes,
    Map<String, String> labels,
    String owner,
    String department,
    String location,
    String environment,
    String description,
    String createdBy,
    String updatedBy,
    Instant createdAt,
    Instant updatedAt
) {}

/**
 * Query parameters for CI search.
 */
public record CIQuery(
    CITypeId typeId,
    CILifecycleStatus status,
    String owner,
    String environment,
    String searchTerm,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {}
