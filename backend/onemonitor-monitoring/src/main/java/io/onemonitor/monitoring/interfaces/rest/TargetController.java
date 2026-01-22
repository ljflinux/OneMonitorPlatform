package io.onemonitor.monitoring.interfaces.rest;

import io.onemonitor.monitoring.application.dto.*;
import io.onemonitor.monitoring.application.service.TargetManagementService;
import io.onemonitor.monitoring.domain.model.Target.TargetStatus;
import io.onemonitor.monitoring.domain.model.Target.TargetType;
import io.onemonitor.monitoring.domain.model.identifier.TargetId;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Target (Monitoring Target) management.
 */
@RestController
@RequestMapping("/api/v1/monitoring/targets")
public class TargetController {

    private static final Logger log = LoggerFactory.getLogger(TargetController.class);

    private final TargetManagementService targetManagementService;

    public TargetController(TargetManagementService targetManagementService) {
        this.targetManagementService = targetManagementService;
    }

    /**
     * Creates a new target.
     */
    @PostMapping
    public ResponseEntity<TargetResponse> createTarget(@Valid @RequestBody TargetCreateRequest request) {
        log.info("Received request to create target: {}", request.name());

        TargetCreateCommand command = new TargetCreateCommand(
            request.name(),
            request.type(),
            request.endpoint(),
            request.port(),
            request.ciId(),
            request.labels(),
            request.description(),
            request.scrapeInterval(),
            request.scrapeTimeout(),
            request.createdBy()
        );

        TargetResponse response = targetManagementService.createTarget(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a target by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TargetResponse> getTarget(@PathVariable String id) {
        log.debug("Received request to get target: {}", id);

        return targetManagementService.getTarget(TargetId.fromString(id))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a target.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TargetResponse> updateTarget(
            @PathVariable String id,
            @Valid @RequestBody TargetUpdateRequest request) {
        log.info("Received request to update target: {}", id);

        TargetUpdateCommand command = new TargetUpdateCommand(
            request.name(),
            request.endpoint(),
            request.port(),
            request.scrapeInterval(),
            request.scrapeTimeout(),
            request.enabled(),
            request.description()
        );

        TargetResponse response = targetManagementService.updateTarget(TargetId.fromString(id), command);
        return ResponseEntity.ok(response);
    }

    /**
     * Enables a target.
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<TargetResponse> enableTarget(@PathVariable String id) {
        log.info("Received request to enable target: {}", id);

        TargetResponse response = targetManagementService.enableTarget(TargetId.fromString(id));
        return ResponseEntity.ok(response);
    }

    /**
     * Disables a target.
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<TargetResponse> disableTarget(@PathVariable String id) {
        log.info("Received request to disable target: {}", id);

        TargetResponse response = targetManagementService.disableTarget(TargetId.fromString(id));
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all targets with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<TargetResponse>> getAllTargets(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ciId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("Received request to list targets with filters");

        Sort sort = sortDirection.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TargetResponse> response;
        
        if (search != null && !search.isBlank()) {
            response = targetManagementService.searchTargets(search, pageable);
        } else {
            TargetType typeEnum = type != null ? TargetType.valueOf(type.toUpperCase()) : null;
            TargetStatus statusEnum = status != null ? TargetStatus.valueOf(status.toUpperCase()) : null;
            response = targetManagementService.findByFilters(typeEnum, statusEnum, ciId, enabled, pageable);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Gets targets that need scraping.
     */
    @GetMapping("/pending-scrape")
    public ResponseEntity<List<TargetResponse>> getTargetsNeedingScrape() {
        log.debug("Received request to get targets needing scrape");

        List<TargetResponse> response = targetManagementService.getTargetsNeedingScrape();
        return ResponseEntity.ok(response);
    }

    /**
     * Associates a target with a CI.
     */
    @PostMapping("/{id}/associate-ci")
    public ResponseEntity<Void> associateWithCI(
            @PathVariable String id,
            @RequestParam String ciId) {
        log.info("Associating target {} with CI {}", id, ciId);

        targetManagementService.associateWithCI(TargetId.fromString(id), ciId);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a target.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTarget(@PathVariable String id) {
        log.info("Received request to delete target: {}", id);

        targetManagementService.deleteTarget(TargetId.fromString(id));
        return ResponseEntity.noContent().build();
    }

    // Request DTOs
    public record TargetCreateRequest(
        String name,
        TargetType type,
        String endpoint,
        Integer port,
        String ciId,
        io.onemonitor.common.domain.vo.LabelSet labels,
        String description,
        Integer scrapeInterval,
        Integer scrapeTimeout,
        String createdBy
    ) {}

    public record TargetUpdateRequest(
        String name,
        String endpoint,
        Integer port,
        Integer scrapeInterval,
        Integer scrapeTimeout,
        Boolean enabled,
        String description
    ) {}
}
