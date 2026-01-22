package io.onemonitor.cmdb.interfaces.rest;

import io.onemonitor.cmdb.application.dto.CICreateCommand;
import io.onemonitor.cmdb.application.dto.CIResponse;
import io.onemonitor.cmdb.application.dto.CIUpdateCommand;
import io.onemonitor.cmdb.application.service.CIManagementService;
import io.onemonitor.cmdb.domain.model.CILifecycleStatus;
import io.onemonitor.cmdb.domain.model.identifier.CIId;
import io.onemonitor.cmdb.domain.model.identifier.CITypeId;
import io.onemonitor.common.domain.vo.LabelSet;
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
import java.util.Map;

/**
 * REST Controller for CI (Configuration Item) management.
 * 
 * Provides endpoints for CRUD operations on CIs.
 */
@RestController
@RequestMapping("/api/v1/cmdb/cis")
public class CIController {

    private static final Logger log = LoggerFactory.getLogger(CIController.class);

    private final CIManagementService ciManagementService;

    public CIController(CIManagementService ciManagementService) {
        this.ciManagementService = ciManagementService;
    }

    /**
     * Creates a new CI.
     */
    @PostMapping
    public ResponseEntity<CIResponse> createCI(@Valid @RequestBody CICreateRequest request) {
        log.info("Received request to create CI with name: {}", request.name());

        CICreateCommand command = new CICreateCommand(
            CITypeId.fromString(request.typeId()),
            request.name(),
            request.displayName(),
            request.attributes(),
            request.labels(),
            request.owner(),
            request.department(),
            request.location(),
            request.environment(),
            request.description(),
            request.createdBy()
        );

        CIResponse response = ciManagementService.createCI(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a CI by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CIResponse> getCI(@PathVariable String id) {
        log.debug("Received request to get CI with ID: {}", id);

        return ciManagementService.getCI(CIId.fromString(id))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing CI.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CIResponse> updateCI(
            @PathVariable String id,
            @Valid @RequestBody CIUpdateRequest request) {
        log.info("Received request to update CI: {}", id);

        CIUpdateCommand command = new CIUpdateCommand(
            request.attributes(),
            request.labels(),
            request.owner(),
            request.displayName(),
            request.department(),
            request.location(),
            request.environment(),
            request.description(),
            request.updatedBy()
        );

        CIResponse response = ciManagementService.updateCI(CIId.fromString(id), command);
        return ResponseEntity.ok(response);
    }

    /**
     * Changes the status of a CI.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<CIResponse> changeStatus(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        log.info("Received request to change status of CI {} to {}", id, status);

        CILifecycleStatus newStatus = CILifecycleStatus.valueOf(status.toUpperCase());
        CIResponse response = ciManagementService.changeStatus(
            CIId.fromString(id), newStatus, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Puts a CI into maintenance mode.
     */
    @PostMapping("/{id}/maintenance")
    public ResponseEntity<CIResponse> enterMaintenance(
            @PathVariable String id,
            @RequestParam String reason) {
        log.info("Received request to put CI {} into maintenance", id);

        CIResponse response = ciManagementService.enterMaintenance(CIId.fromString(id), reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a CI to active status.
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<CIResponse> returnToActive(
            @PathVariable String id,
            @RequestParam(required = false) String reason) {
        log.info("Received request to activate CI {}", id);

        CIResponse response = ciManagementService.returnToActive(CIId.fromString(id), reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Decommission a CI.
     */
    @PostMapping("/{id}/decommission")
    public ResponseEntity<CIResponse> decommission(
            @PathVariable String id,
            @RequestParam String reason) {
        log.info("Received request to decommission CI {}", id);

        CIResponse response = ciManagementService.decommission(CIId.fromString(id), reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all CIs with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<CIResponse>> getAllCIs(
            @RequestParam(required = false) String typeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.debug("Received request to list CIs with filters");

        Sort sort = sortDirection.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CIResponse> response;
        
        if (search != null && !search.isBlank()) {
            response = ciManagementService.searchCIs(search, pageable);
        } else {
            CITypeId type = typeId != null ? CITypeId.fromString(typeId) : null;
            CILifecycleStatus stat = status != null ? CILifecycleStatus.valueOf(status.toUpperCase()) : null;
            response = ciManagementService.findByFilters(type, stat, owner, environment, pageable);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Gets multiple CIs by IDs.
     */
    @PostMapping("/batch")
    public ResponseEntity<List<CIResponse>> getCIsByIds(@RequestBody List<String> ids) {
        log.debug("Received request to get {} CIs by IDs", ids.size());

        List<CIId> ciIds = ids.stream()
            .map(CIId::fromString)
            .toList();
        
        List<CIResponse> response = ciManagementService.getCIsByIds(ciIds);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets correlation labels for a CI.
     */
    @GetMapping("/{id}/labels")
    public ResponseEntity<Map<String, String>> getCorrelationLabels(@PathVariable String id) {
        log.debug("Received request to get correlation labels for CI: {}", id);

        LabelSet labels = ciManagementService.getCorrelationLabels(CIId.fromString(id));
        return ResponseEntity.ok(labels.asMap());
    }

    /**
     * Deletes a CI.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCI(@PathVariable String id) {
        log.info("Received request to delete CI: {}", id);

        ciManagementService.deleteCI(CIId.fromString(id));
        return ResponseEntity.noContent().build();
    }

    // Request DTOs
    public record CICreateRequest(
        String typeId,
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

    public record CIUpdateRequest(
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
}
