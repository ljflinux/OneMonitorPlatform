package io.onemonitor.alerting.interfaces.rest;

import io.onemonitor.alerting.application.dto.*;
import io.onemonitor.alerting.application.service.AlertingManagementService;
import io.onemonitor.alerting.domain.model.AlertInstance;
import io.onemonitor.alerting.domain.model.AlertRule;
import io.onemonitor.alerting.domain.model.identifier.AlertInstanceId;
import io.onemonitor.alerting.domain.model.identifier.AlertRuleId;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Alerting management.
 * 
 * Provides endpoints for Alert Rule and Alert Instance management.
 */
@RestController
@RequestMapping("/api/v1/alerting")
public class AlertingController {

    private static final Logger log = LoggerFactory.getLogger(AlertingController.class);

    private final AlertingManagementService alertingService;

    public AlertingController(AlertingManagementService alertingService) {
        this.alertingService = alertingService;
    }

    // ==================== Alert Rule Endpoints ====================

    /**
     * Creates a new Alert Rule.
     */
    @PostMapping("/rules")
    public ResponseEntity<AlertRuleResponse> createAlertRule(
            @Valid @RequestBody AlertRuleCreateRequest request) {
        log.info("Received request to create alert rule: {}", request.name());

        AlertRuleCreateCommand command = new AlertRuleCreateCommand(
            request.name(),
            request.displayName(),
            request.description(),
            AlertRule.AlertSeverity.valueOf(request.severity().toUpperCase()),
            request.promqlExpression(),
            request.evaluationInterval(),
            request.forDuration(),
            request.summaryTemplate(),
            request.descriptionTemplate(),
            request.runbookUrl(),
            request.labels(),
            request.ciId(),
            request.notificationChannelIds(),
            request.autoResolve(),
            request.resolveTimeout(),
            request.createdBy()
        );

        AlertRuleResponse response = alertingService.createAlertRule(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets an Alert Rule by ID.
     */
    @GetMapping("/rules/{id}")
    public ResponseEntity<AlertRuleResponse> getAlertRule(@PathVariable String id) {
        log.debug("Received request to get alert rule: {}", id);

        return alertingService.getAlertRule(AlertRuleId.fromString(id))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an Alert Rule.
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<AlertRuleResponse> updateAlertRule(
            @PathVariable String id,
            @Valid @RequestBody AlertRuleUpdateRequest request) {
        log.info("Received request to update alert rule: {}", id);

        AlertRuleUpdateCommand command = new AlertRuleUpdateCommand(
            request.description(),
            request.severity() != null 
                ? AlertRule.AlertSeverity.valueOf(request.severity().toUpperCase()) 
                : null,
            request.promqlExpression(),
            request.evaluationInterval(),
            request.forDuration(),
            request.summaryTemplate(),
            request.descriptionTemplate(),
            request.runbookUrl(),
            request.labels(),
            request.notificationChannelIds(),
            request.autoResolve(),
            request.resolveTimeout(),
            request.enabled()
        );

        AlertRuleResponse response = alertingService.updateAlertRule(
            AlertRuleId.fromString(id), command);
        return ResponseEntity.ok(response);
    }

    /**
     * Enables an Alert Rule.
     */
    @PostMapping("/rules/{id}/enable")
    public ResponseEntity<AlertRuleResponse> enableAlertRule(@PathVariable String id) {
        log.info("Received request to enable alert rule: {}", id);

        AlertRuleResponse response = alertingService.enableAlertRule(AlertRuleId.fromString(id));
        return ResponseEntity.ok(response);
    }

    /**
     * Disables an Alert Rule.
     */
    @PostMapping("/rules/{id}/disable")
    public ResponseEntity<AlertRuleResponse> disableAlertRule(@PathVariable String id) {
        log.info("Received request to disable alert rule: {}", id);

        AlertRuleResponse response = alertingService.disableAlertRule(AlertRuleId.fromString(id));
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an Alert Rule.
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteAlertRule(@PathVariable String id) {
        log.info("Received request to delete alert rule: {}", id);

        alertingService.deleteAlertRule(AlertRuleId.fromString(id));
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets all Alert Rules with pagination and filtering.
     */
    @GetMapping("/rules")
    public ResponseEntity<Page<AlertRuleResponse>> getAlertRules(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String ciId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.debug("Received request to list alert rules with filters");

        Sort sort = sortDirection.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        AlertRule.AlertSeverity severityEnum = severity != null 
            ? AlertRule.AlertSeverity.valueOf(severity.toUpperCase()) 
            : null;

        Page<AlertRuleResponse> response = alertingService.findAlertRules(
            severityEnum, enabled, ciId, search, pageable);

        return ResponseEntity.ok(response);
    }

    // ==================== Alert Instance Endpoints ====================

    /**
     * Gets an Alert Instance by ID.
     */
    @GetMapping("/instances/{id}")
    public ResponseEntity<AlertInstanceResponse> getAlertInstance(@PathVariable String id) {
        log.debug("Received request to get alert instance: {}", id);

        return alertingService.getAlertInstance(AlertInstanceId.fromString(id))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Acknowledges an Alert.
     */
    @PostMapping("/instances/{id}/acknowledge")
    public ResponseEntity<AlertInstanceResponse> acknowledgeAlert(
            @PathVariable String id,
            @Valid @RequestBody AlertAckRequest request) {
        log.info("Received request to acknowledge alert: {}", id);

        AlertAckCommand command = new AlertAckCommand(request.userId(), request.comment());
        AlertInstanceResponse response = alertingService.acknowledgeAlert(
            AlertInstanceId.fromString(id), command);
        return ResponseEntity.ok(response);
    }

    /**
     * Resolves an Alert.
     */
    @PostMapping("/instances/{id}/resolve")
    public ResponseEntity<AlertInstanceResponse> resolveAlert(
            @PathVariable String id,
            @Valid @RequestBody AlertResolveRequest request) {
        log.info("Received request to resolve alert: {}", id);

        AlertResolveCommand command = new AlertResolveCommand(
            request.userId(), request.comment(), request.autoResolved());
        AlertInstanceResponse response = alertingService.resolveAlert(
            AlertInstanceId.fromString(id), command);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets Alert Instances with pagination and filtering.
     */
    @GetMapping("/instances")
    public ResponseEntity<Page<AlertInstanceResponse>> getAlertInstances(
            @RequestParam(required = false) String ruleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String ciId,
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.debug("Received request to list alert instances with filters");

        Sort sort = sortDirection.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        AlertInstance.AlertStatus statusEnum = status != null 
            ? AlertInstance.AlertStatus.valueOf(status.toUpperCase()) 
            : null;
        AlertRule.AlertSeverity severityEnum = severity != null 
            ? AlertRule.AlertSeverity.valueOf(severity.toUpperCase()) 
            : null;

        Page<AlertInstanceResponse> response = alertingService.findAlertInstances(
            ruleId, statusEnum, severityEnum, ciId, startTime, endTime, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Gets active (firing) alerts.
     */
    @GetMapping("/alerts/active")
    public ResponseEntity<Page<AlertInstanceResponse>> getActiveAlerts(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String ciId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Received request to get active alerts");

        Pageable pageable = PageRequest.of(page, size, Sort.by("firedAt").descending());

        AlertRule.AlertSeverity severityEnum = severity != null 
            ? AlertRule.AlertSeverity.valueOf(severity.toUpperCase()) 
            : null;

        Page<AlertInstanceResponse> response = alertingService.getActiveAlerts(
            severityEnum, ciId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Gets alert statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<AlertStatsResponse> getAlertStats() {
        log.debug("Received request to get alert statistics");

        AlertStatsResponse stats = alertingService.getAlertStats();
        return ResponseEntity.ok(stats);
    }

    // ==================== Request/Response DTOs ====================

    public record AlertRuleCreateRequest(
        String name,
        String displayName,
        String description,
        String severity,
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

    public record AlertRuleUpdateRequest(
        String description,
        String severity,
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

    public record AlertAckRequest(
        String userId,
        String comment
    ) {}

    public record AlertResolveRequest(
        String userId,
        String comment,
        boolean autoResolved
    ) {}
}
