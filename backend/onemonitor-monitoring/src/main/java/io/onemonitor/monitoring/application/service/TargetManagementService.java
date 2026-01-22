package io.onemonitor.monitoring.application.service;

import io.onemonitor.common.domain.vo.LabelSet;
import io.onemonitor.monitoring.application.dto.TargetCreateCommand;
import io.onemonitor.monitoring.application.dto.TargetResponse;
import io.onemonitor.monitoring.application.dto.TargetUpdateCommand;
import io.onemonitor.monitoring.application.mapper.TargetMapper;
import io.onemonitor.monitoring.domain.event.*;
import io.onemonitor.monitoring.domain.model.Target;
import io.onemonitor.monitoring.domain.model.Target.TargetStatus;
import io.onemonitor.monitoring.domain.model.Target.TargetType;
import io.onemonitor.monitoring.domain.model.identifier.TargetId;
import io.onemonitor.monitoring.domain.repository.TargetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Application service for Target management operations.
 */
@Service
@Transactional
public class TargetManagementService {

    private static final Logger log = LoggerFactory.getLogger(TargetManagementService.class);

    private final TargetRepository targetRepository;
    private final TargetMapper targetMapper;
    private final ApplicationEventPublisher eventPublisher;

    public TargetManagementService(TargetRepository targetRepository,
                                   TargetMapper targetMapper,
                                   ApplicationEventPublisher eventPublisher) {
        this.targetRepository = targetRepository;
        this.targetMapper = targetMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new monitoring target.
     */
    public TargetResponse createTarget(TargetCreateCommand command) {
        log.info("Creating target: {} ({})", command.name(), command.endpoint());

        // Check for duplicate endpoint
        if (targetRepository.existsByEndpoint(command.endpoint())) {
            throw new DuplicateTargetException(
                "Target with endpoint '" + command.endpoint() + "' already exists");
        }

        // Create target aggregate
        LabelSet labels = command.labels() != null ? command.labels() : LabelSet.empty();
        Target target = Target.create(
            command.name(),
            command.type(),
            command.endpoint(),
            command.port(),
            command.ciId(),
            labels
        );

        // Save aggregate
        Target savedTarget = targetRepository.save(target);

        // Publish domain event
        publishDomainEvent(new TargetCreated(
            savedTarget.getId(),
            savedTarget.getName(),
            savedTarget.getType(),
            savedTarget.getEndpoint(),
            savedTarget.getCiId(),
            savedTarget.getCreatedAt()
        ));

        log.info("Created target with ID: {}", savedTarget.getId().getValue());
        return targetMapper.toResponse(savedTarget);
    }

    /**
     * Updates an existing target.
     */
    public TargetResponse updateTarget(TargetId targetId, TargetUpdateCommand command) {
        log.info("Updating target: {}", targetId.getValue());

        Target target = targetRepository.findById(targetId)
            .orElseThrow(() -> new TargetNotFoundException("Target not found: " + targetId));

        target.updateConfig(
            command.name(),
            command.endpoint(),
            command.port(),
            command.scrapeInterval(),
            command.scrapeTimeout(),
            command.enabled() != null ? command.enabled() : target.isEnabled()
        );

        Target savedTarget = targetRepository.save(target);
        publishDomainEvent(new TargetStatusChanged(
            savedTarget.getId(),
            savedTarget.getName(),
            null, // status not changed in update
            savedTarget.getStatus(),
            "Configuration updated",
            Instant.now()
        ));

        log.info("Updated target: {}", targetId.getValue());
        return targetMapper.toResponse(savedTarget);
    }

    /**
     * Records a successful scrape.
     */
    public void recordScrapeSuccess(TargetId targetId, int metricsCount) {
        Target target = targetRepository.findById(targetId)
            .orElseThrow(() -> new TargetNotFoundException("Target not found: " + targetId));

        target.recordScrapeSuccess();
        
        // Calculate next scrape time
        Instant nextScrape = Instant.now().plus(
            target.getScrapeInterval() != null ? target.getScrapeInterval() : 15,
            ChronoUnit.SECONDS
        );
        target.setNextScrape(nextScrape);
        
        targetRepository.save(target);

        publishDomainEvent(new ScrapeSuccess(
            targetId,
            target.getEndpoint(),
            metricsCount,
            Instant.now()
        ));

        log.debug("Recorded scrape success for target: {} ({} metrics)", 
                  targetId.getValue(), metricsCount);
    }

    /**
     * Records a failed scrape.
     */
    public void recordScrapeFailure(TargetId targetId, String errorMessage) {
        Target target = targetRepository.findById(targetId)
            .orElseThrow(() -> new TargetNotFoundException("Target not found: " + targetId));

        TargetStatus oldStatus = target.getStatus();
        target.recordScrapeFailure(errorMessage);
        Target savedTarget = targetRepository.save(target);

        publishDomainEvent(new ScrapeFailure(
            targetId,
            target.getEndpoint(),
            errorMessage,
            Instant.now()
        ));

        // Publish status change if status changed
        if (oldStatus != savedTarget.getStatus()) {
            publishDomainEvent(new TargetStatusChanged(
                targetId,
                target.getName(),
                oldStatus,
                savedTarget.getStatus(),
                "Scrape failed: " + errorMessage,
                Instant.now()
            ));
        }

        log.warn("Recorded scrape failure for target: {} - {}", 
                 targetId.getValue(), errorMessage);
    }

    /**
     * Enables a target.
     */
    public TargetResponse enableTarget(TargetId targetId) {
        Target target = targetRepository.findById(targetId)
            .orElseThrow(() -> new TargetNotFoundException("Target not found: " + targetId));

        target.enable();
        Target savedTarget = targetRepository.save(target);

        publishDomainEvent(new TargetStatusChanged(
            targetId,
            savedTarget.getName(),
            TargetStatus.DISABLED,
            TargetStatus.UNKNOWN,
            "Target enabled",
            Instant.now()
        ));

        return targetMapper.toResponse(savedTarget);
    }

    /**
     * Disables a target.
     */
    public TargetResponse disableTarget(TargetId targetId) {
        Target target = targetRepository.findById(targetId)
            .orElseThrow(() -> new TargetNotFoundException("Target not found: " + targetId));

        TargetStatus oldStatus = target.getStatus();
        target.setEnabled(false);
        Target savedTarget = targetRepository.save(target);

        publishDomainEvent(new TargetStatusChanged(
            targetId,
            savedTarget.getName(),
            oldStatus,
            TargetStatus.DISABLED,
            "Target disabled",
            Instant.now()
        ));

        return targetMapper.toResponse(savedTarget);
    }

    /**
     * Gets a target by ID.
     */
    @Transactional(readOnly = true)
    public Optional<TargetResponse> getTarget(TargetId targetId) {
        return targetRepository.findById(targetId)
            .map(targetMapper::toResponse);
    }

    /**
     * Gets all targets with pagination.
     */
    @Transactional(readOnly = true)
    public Page<TargetResponse> getAllTargets(Pageable pageable) {
        return targetRepository.findAll(pageable)
            .map(targetMapper::toResponse);
    }

    /**
     * Gets targets by type.
     */
    @Transactional(readOnly = true)
    public Page<TargetResponse> getTargetsByType(TargetType type, Pageable pageable) {
        return targetRepository.findByType(type, pageable)
            .map(targetMapper::toResponse);
    }

    /**
     * Gets targets by status.
     */
    @Transactional(readOnly = true)
    public Page<TargetResponse> getTargetsByStatus(TargetStatus status, Pageable pageable) {
        return targetRepository.findByStatus(status, pageable)
            .map(targetMapper::toResponse);
    }

    /**
     * Gets targets that need scraping.
     */
    @Transactional(readOnly = true)
    public List<TargetResponse> getTargetsNeedingScrape() {
        return targetRepository.findTargetsNeedingScrape(Instant.now())
            .stream()
            .map(targetMapper::toResponse)
            .toList();
    }

    /**
     * Searches targets.
     */
    @Transactional(readOnly = true)
    public Page<TargetResponse> searchTargets(String searchTerm, Pageable pageable) {
        return targetRepository.searchByNameOrEndpoint(searchTerm, pageable)
            .map(targetMapper::toResponse);
    }

    /**
     * Gets targets with filters.
     */
    @Transactional(readOnly = true)
    public Page<TargetResponse> findByFilters(TargetType type, TargetStatus status,
                                               String ciId, Boolean enabled, Pageable pageable) {
        return targetRepository.findByFilters(type, status, ciId, enabled, pageable)
            .map(targetMapper::toResponse);
    }

    /**
     * Associates a target with a CI.
     */
    public void associateWithCI(TargetId targetId, String ciId) {
        Target target = targetRepository.findById(targetId)
            .orElseThrow(() -> new TargetNotFoundException("Target not found: " + targetId));

        target.associateWithCI(ciId);
        targetRepository.save(target);
    }

    /**
     * Deletes a target.
     */
    public void deleteTarget(TargetId targetId) {
        log.info("Deleting target: {}", targetId.getValue());

        if (!targetRepository.findById(targetId).isPresent()) {
            throw new TargetNotFoundException("Target not found: " + targetId);
        }

        targetRepository.deleteById(targetId);
        log.info("Deleted target: {}", targetId.getValue());
    }

    /**
     * Publishes domain event to event bus.
     */
    private void publishDomainEvent(MonitoringEvent event) {
        eventPublisher.publishEvent(event);
    }

    // Exception classes
    public static class TargetNotFoundException extends RuntimeException {
        public TargetNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateTargetException extends RuntimeException {
        public DuplicateTargetException(String message) {
            super(message);
        }
    }
}
