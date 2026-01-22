package io.onemonitor.alerting.application.service;

import io.onemonitor.alerting.application.dto.*;
import io.onemonitor.alerting.application.mapper.AlertingMapper;
import io.onemonitor.alerting.domain.event.*;
import io.onemonitor.alerting.domain.model.*;
import io.onemonitor.alerting.domain.model.AlertInstance.AlertStatus;
import io.onemonitor.alerting.domain.model.AlertRule.AlertSeverity;
import io.onemonitor.alerting.domain.model.identifier.AlertInstanceId;
import io.onemonitor.alerting.domain.model.identifier.AlertRuleId;
import io.onemonitor.alerting.domain.repository.AlertInstanceRepository;
import io.onemonitor.alerting.domain.repository.AlertRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Application service for Alerting management operations.
 */
@Service
@Transactional
public class AlertingManagementService {

    private static final Logger log = LoggerFactory.getLogger(AlertingManagementService.class);

    private final AlertRuleRepository alertRuleRepository;
    private final AlertInstanceRepository alertInstanceRepository;
    private final AlertingMapper alertingMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AlertingManagementService(AlertRuleRepository alertRuleRepository,
                                    AlertInstanceRepository alertInstanceRepository,
                                    AlertingMapper alertingMapper,
                                    ApplicationEventPublisher eventPublisher) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertInstanceRepository = alertInstanceRepository;
        this.alertingMapper = alertingMapper;
        this.eventPublisher = eventPublisher;
    }

    // ==================== Alert Rule Operations ====================

    /**
     * Creates a new alert rule.
     */
    public AlertRuleResponse createAlertRule(AlertRuleCreateCommand command) {
        log.info("Creating alert rule: {}", command.name());

        if (alertRuleRepository.existsByName(command.name())) {
            throw new DuplicateAlertRuleException(
                "Alert rule with name '" + command.name() + "' already exists");
        }

        AlertRule rule = AlertRule.create(
            command.name(),
            command.promqlExpression(),
            command.severity(),
            command.description()
        );

        if (command.displayName() != null) {
            rule.setDisplayName(command.displayName());
        }
        if (command.evaluationInterval() != null) {
            rule.setEvaluationInterval(command.evaluationInterval());
        }
        if (command.forDuration() != null) {
            rule.setForDuration(command.forDuration());
        }
        if (command.summaryTemplate() != null) {
            rule.setSummaryTemplate(command.summaryTemplate());
        }
        if (command.descriptionTemplate() != null) {
            rule.setDescriptionTemplate(command.descriptionTemplate());
        }
        if (command.runbookUrl() != null) {
            rule.setRunbookUrl(command.runbookUrl());
        }
        if (command.labels() != null) {
            rule.setLabels(command.labels());
        }
        if (command.ciId() != null) {
            rule.setCiId(command.ciId());
        }
        if (command.notificationChannelIds() != null) {
            rule.setNotificationChannels(command.notificationChannelIds());
        }
        if (command.autoResolve() != null) {
            rule.setAutoResolve(command.autoResolve());
        }
        if (command.resolveTimeout() != null) {
            rule.setResolveTimeout(command.resolveTimeout());
        }
        rule.setCreatedBy(command.createdBy());

        AlertRule savedRule = alertRuleRepository.save(rule);

        publishDomainEvent(new AlertRuleCreated(
            savedRule.getId(),
            savedRule.getName(),
            savedRule.getSeverity(),
            savedRule.getCiId(),
            savedRule.getCreatedAt()
        ));

        log.info("Created alert rule with ID: {}", savedRule.getId().getValue());
        return alertingMapper.toRuleResponse(savedRule);
    }

    /**
     * Updates an existing alert rule.
     */
    public AlertRuleResponse updateAlertRule(AlertRuleId ruleId, AlertRuleUpdateCommand command) {
        log.info("Updating alert rule: {}", ruleId.getValue());

        AlertRule rule = alertRuleRepository.findById(ruleId)
            .orElseThrow(() -> new AlertRuleNotFoundException("Alert rule not found: " + ruleId));

        String modifiedFields = trackModifiedFields(rule, command);

        rule.update(
            command.promqlExpression(),
            command.severity(),
            command.description(),
            command.evaluationInterval(),
            command.forDuration(),
            command.enabled()
        );

        if (command.summaryTemplate() != null || command.descriptionTemplate() != null ||
            command.runbookUrl() != null) {
            rule.updateTemplates(
                command.summaryTemplate(),
                command.descriptionTemplate(),
                command.runbookUrl()
            );
        }

        if (command.labels() != null) {
            command.labels().forEach(rule::addLabel);
        }

        if (command.notificationChannelIds() != null) {
            rule.setNotificationChannels(command.notificationChannelIds());
        }

        if (command.autoResolve() != null) {
            rule.setAutoResolve(command.autoResolve());
        }

        if (command.resolveTimeout() != null) {
            rule.setResolveTimeout(command.resolveTimeout());
        }

        AlertRule savedRule = alertRuleRepository.save(rule);

        if (!modifiedFields.isEmpty()) {
            publishDomainEvent(new AlertRuleModified(
                ruleId,
                savedRule.getName(),
                modifiedFields,
                Instant.now()
            ));
        }

        log.info("Updated alert rule: {}", ruleId.getValue());
        return alertingMapper.toRuleResponse(savedRule);
    }

    private String trackModifiedFields(AlertRule rule, AlertRuleUpdateCommand command) {
        List<String> fields = new ArrayList<>();
        if (command.promqlExpression() != null) return "expression";
        if (command.severity() != null) return "severity";
        if (command.description() != null) return "description";
        if (command.evaluationInterval() != null) return "evaluationInterval";
        if (command.forDuration() != null) return "forDuration";
        if (command.enabled() != null) return "enabled";
        if (command.summaryTemplate() != null) return "summaryTemplate";
        if (command.descriptionTemplate() != null) return "descriptionTemplate";
        if (command.runbookUrl() != null) return "runbookUrl";
        if (command.labels() != null) return "labels";
        if (command.notificationChannelIds() != null) return "notificationChannels";
        if (command.autoResolve() != null) return "autoResolve";
        if (command.resolveTimeout() != null) return "resolveTimeout";
        return String.join(",", fields);
    }

    /**
     * Enables an alert rule.
     */
    public AlertRuleResponse enableAlertRule(AlertRuleId ruleId) {
        AlertRule rule = alertRuleRepository.findById(ruleId)
            .orElseThrow(() -> new AlertRuleNotFoundException("Alert rule not found: " + ruleId));

        rule.enable();
        AlertRule savedRule = alertRuleRepository.save(rule);

        publishDomainEvent(new AlertRuleEnabledChanged(
            ruleId, savedRule.getName(), true, Instant.now()
        ));

        return alertingMapper.toRuleResponse(savedRule);
    }

    /**
     * Disables an alert rule.
     */
    public AlertRuleResponse disableAlertRule(AlertRuleId ruleId) {
        AlertRule rule = alertRuleRepository.findById(ruleId)
            .orElseThrow(() -> new AlertRuleNotFoundException("Alert rule not found: " + ruleId));

        rule.disable();
        AlertRule savedRule = alertRuleRepository.save(rule);

        publishDomainEvent(new AlertRuleEnabledChanged(
            ruleId, savedRule.getName(), false, Instant.now()
        ));

        return alertingMapper.toRuleResponse(savedRule);
    }

    /**
     * Gets an alert rule by ID.
     */
    @Transactional(readOnly = true)
    public Optional<AlertRuleResponse> getAlertRule(AlertRuleId ruleId) {
        return alertRuleRepository.findById(ruleId)
            .map(alertingMapper::toRuleResponse);
    }

    /**
     * Gets all alert rules with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AlertRuleResponse> getAllAlertRules(Pageable pageable) {
        return alertRuleRepository.findAll(pageable)
            .map(alertingMapper::toRuleResponse);
    }

    /**
     * Gets alert rules by filters.
     */
    @Transactional(readOnly = true)
    public Page<AlertRuleResponse> findAlertRules(AlertRuleQuery query) {
        Sort sort = query.sortDirection().equalsIgnoreCase("asc") 
            ? Sort.by(query.sortBy()).ascending() 
            : Sort.by(query.sortBy()).descending();
        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        Page<AlertRule> result;
        
        if (query.searchTerm() != null && !query.searchTerm().isBlank()) {
            result = alertRuleRepository.searchByNameOrDescription(query.searchTerm(), pageable);
        } else {
            result = alertRuleRepository.findByFilters(
                query.severity(), query.enabled(), query.ciId(), pageable);
        }

        return result.map(alertingMapper::toRuleResponse);
    }

    /**
     * Gets enabled alert rules for evaluation.
     */
    @Transactional(readOnly = true)
    public List<AlertRuleResponse> getEnabledAlertRules() {
        return alertRuleRepository.findByEnabledTrue().stream()
            .map(alertingMapper::toRuleResponse)
            .toList();
    }

    /**
     * Deletes an alert rule.
     */
    public void deleteAlertRule(AlertRuleId ruleId) {
        log.info("Deleting alert rule: {}", ruleId.getValue());

        if (!alertRuleRepository.findById(ruleId).isPresent()) {
            throw new AlertRuleNotFoundException("Alert rule not found: " + ruleId);
        }

        alertRuleRepository.deleteById(ruleId);
        log.info("Deleted alert rule: {}", ruleId.getValue());
    }

    // ==================== Alert Instance Operations ====================

    /**
     * Fires an alert based on rule evaluation.
     */
    public AlertInstance fireAlert(AlertFireCommand command) {
        log.debug("Firing alert for rule: {}, fingerprint: {}", 
                  command.ruleId(), command.fingerprint());

        // Check for existing alert with same fingerprint
        Optional<AlertInstance> existing = 
            alertInstanceRepository.findByFingerprint(command.fingerprint());

        AlertInstance instance;
        
        if (existing.isPresent()) {
            // Alert already exists - check if it needs to be re-fired
            AlertInstance existingAlert = existing.get();
            if (existingAlert.getStatus() == AlertStatus.RESOLVED) {
                existingAlert.refire();
                instance = alertInstanceRepository.save(existingAlert);
            } else {
                // Already firing or acknowledged - just update current value
                existingAlert.setCurrentValue(String.valueOf(command.currentValue()));
                instance = alertInstanceRepository.save(existingAlert);
            }
        } else {
            // Create new alert instance
            AlertRule rule = alertRuleRepository.findById(AlertRuleId.fromString(command.ruleId()))
                .orElseThrow(() -> new AlertRuleNotFoundException(
                    "Alert rule not found: " + command.ruleId()));

            instance = AlertInstance.fire(
                rule,
                command.fingerprint(),
                command.labels(),
                command.values(),
                command.currentValue()
            );

            if (rule.getCiId() != null) {
                instance.setCiId(rule.getCiId());
            }

            rule.recordFired();
            alertRuleRepository.save(rule);
            instance = alertInstanceRepository.save(instance);
        }

        publishDomainEvent(new AlertFired(
            instance.getId(),
            instance.getRuleId(),
            instance.getRuleName(),
            instance.getSeverity(),
            instance.getCiId(),
            parseLabels(command.labels()),
            command.currentValue(),
            instance.getSummary(),
            instance.getFiredAt()
        ));

        return instance;
    }

    /**
     * Acknowledges an alert.
     */
    public void acknowledgeAlert(AlertInstanceId alertId, AlertAckCommand command) {
        log.info("Acknowledging alert: {}", alertId.getValue());

        AlertInstance instance = alertInstanceRepository.findById(alertId)
            .orElseThrow(() -> new AlertInstanceNotFoundException(
                "Alert instance not found: " + alertId));

        if (!instance.canAcknowledge()) {
            throw new InvalidAlertOperationException(
                "Cannot acknowledge alert in status: " + instance.getStatus());
        }

        instance.acknowledge(command.userId(), command.comment());
        AlertInstance savedInstance = alertInstanceRepository.save(instance);

        publishDomainEvent(new AlertAcknowledged(
            alertId,
            savedInstance.getRuleId(),
            savedInstance.getRuleName(),
            command.userId(),
            command.comment(),
            Instant.now()
        ));

        log.info("Acknowledged alert: {}", alertId.getValue());
    }

    /**
     * Resolves an alert.
     */
    public void resolveAlert(AlertInstanceId alertId, AlertResolveCommand command) {
        log.info("Resolving alert: {}", alertId.getValue());

        AlertInstance instance = alertInstanceRepository.findById(alertId)
            .orElseThrow(() -> new AlertInstanceNotFoundException(
                "Alert instance not found: " + alertId));

        if (!instance.canResolve()) {
            throw new InvalidAlertOperationException(
                "Cannot resolve alert in status: " + instance.getStatus());
        }

        instance.resolve(command.userId(), command.comment(), command.autoResolved());
        AlertInstance savedInstance = alertInstanceRepository.save(instance);

        publishDomainEvent(new AlertResolved(
            alertId,
            savedInstance.getRuleId(),
            savedInstance.getRuleName(),
            command.userId(),
            command.comment(),
            command.autoResolved(),
            instance.getElapsedSeconds(),
            Instant.now()
        ));

        log.info("Resolved alert: {}", alertId.getValue());
    }

    /**
     * Gets an alert instance by ID.
     */
    @Transactional(readOnly = true)
    public Optional<AlertInstance> getAlertInstance(AlertInstanceId alertId) {
        return alertInstanceRepository.findById(alertId);
    }

    /**
     * Gets firing alerts.
     */
    @Transactional(readOnly = true)
    public Page<AlertInstance> getFiringAlerts(Pageable pageable) {
        return alertInstanceRepository.findByStatus(AlertStatus.FIRING, pageable);
    }

    /**
     * Gets alert instances by filters.
     */
    @Transactional(readOnly = true)
    public Page<AlertInstance> findAlertInstances(AlertInstanceQuery query) {
        Sort sort = query.sortBy() != null ? 
            (query.sortDirection().equalsIgnoreCase("asc") 
                ? Sort.by(query.sortBy()).ascending() 
                : Sort.by(query.sortBy()).descending()) 
            : Sort.by("firedAt").descending();
        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        return alertInstanceRepository.findByFilters(
            query.ruleId() != null ? AlertRuleId.fromString(query.ruleId()) : null,
            query.status() != null ? AlertStatus.valueOf(query.status()) : null,
            query.severity() != null ? AlertSeverity.valueOf(query.severity()) : null,
            query.ciId(),
            pageable
        );
    }

    /**
     * Gets alert statistics.
     */
    @Transactional(readOnly = true)
    public AlertStatistics getAlertStatistics() {
        long firing = alertInstanceRepository.countByStatus(AlertStatus.FIRING);
        long acknowledged = alertInstanceRepository.countByStatus(AlertStatus.ACKNOWLEDGED);
        long resolved = alertInstanceRepository.countByStatus(AlertStatus.RESOLVED);

        Map<String, Long> bySeverity = new HashMap<>();
        for (AlertSeverity severity : AlertSeverity.values()) {
            bySeverity.put(severity.name(), 
                alertInstanceRepository.countBySeverityAndStatus(severity, AlertStatus.FIRING));
        }

        return new AlertStatistics(firing, acknowledged, resolved, bySeverity);
    }

    // ==================== Helpers ====================

    private void publishDomainEvent(AlertingEvent event) {
        eventPublisher.publishEvent(event);
    }

    private Map<String, String> parseLabels(Map<String, String> labels) {
        return labels != null ? labels : Collections.emptyMap();
    }

    // Exception classes
    public static class AlertRuleNotFoundException extends RuntimeException {
        public AlertRuleNotFoundException(String message) { super(message); }
    }

    public static class AlertInstanceNotFoundException extends RuntimeException {
        public AlertInstanceNotFoundException(String message) { super(message); }
    }

    public static class DuplicateAlertRuleException extends RuntimeException {
        public DuplicateAlertRuleException(String message) { super(message); }
    }

    public static class InvalidAlertOperationException extends RuntimeException {
        public InvalidAlertOperationException(String message) { super(message); }
    }

    // Statistics record
    public record AlertStatistics(
        long firing,
        long acknowledged,
        long resolved,
        Map<String, Long> bySeverity
    ) {}
}
