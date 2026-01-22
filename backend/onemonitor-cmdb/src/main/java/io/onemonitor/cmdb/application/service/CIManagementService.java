package io.onemonitor.cmdb.application.service;

import io.onemonitor.cmdb.application.dto.CICreateCommand;
import io.onemonitor.cmdb.application.dto.CIUpdateCommand;
import io.onemonitor.cmdb.application.dto.CIResponse;
import io.onemonitor.cmdb.application.mapper.CIMapper;
import io.onemonitor.cmdb.domain.event.DomainEvent;
import io.onemonitor.cmdb.domain.model.CI;
import io.onemonitor.cmdb.domain.model.CILifecycleStatus;
import io.onemonitor.cmdb.domain.model.identifier.CIId;
import io.onemonitor.cmdb.domain.model.identifier.CITypeId;
import io.onemonitor.cmdb.domain.repository.CIRepository;
import io.onemonitor.common.domain.vo.LabelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Application service for CI management operations.
 * 
 * This service orchestrates CI domain operations and handles
 * business logic that spans multiple aggregates.
 */
@Service
@Transactional
public class CIManagementService {

    private static final Logger log = LoggerFactory.getLogger(CIManagementService.class);

    private final CIRepository ciRepository;
    private final CIMapper ciMapper;
    private final ApplicationEventPublisher eventPublisher;

    public CIManagementService(CIRepository ciRepository, 
                               CIMapper ciMapper,
                               ApplicationEventPublisher eventPublisher) {
        this.ciRepository = ciRepository;
        this.ciMapper = ciMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new CI.
     */
    public CIResponse createCI(CICreateCommand command) {
        log.info("Creating CI with type {} and name {}", command.typeId(), command.name());

        // Check for duplicate name within type
        if (ciRepository.existsByTypeIdAndName(command.typeId(), command.name().toLowerCase())) {
            throw new DuplicateCIException(
                "CI with name '" + command.name() + "' already exists for type " + command.typeId());
        }

        // Create CI aggregate
        CI ci = CI.create(
            command.typeId(),
            command.name(),
            command.attributes(),
            command.labels(),
            command.owner(),
            command.environment()
        );

        // Save aggregate
        CI savedCI = ciRepository.save(ci);

        // Publish domain events
        publishDomainEvents(savedCI);

        log.info("Created CI with ID {}", savedCI.getId().getValue());
        return ciMapper.toResponse(savedCI);
    }

    /**
     * Updates an existing CI.
     */
    public CIResponse updateCI(CIId ciId, CIUpdateCommand command) {
        log.info("Updating CI {}", ciId.getValue());

        CI ci = ciRepository.findById(ciId)
            .orElseThrow(() -> new CINotFoundException("CI not found: " + ciId));

        // Update attributes
        if (command.attributes() != null && !command.attributes().isEmpty()) {
            ci.updateAttributes(command.attributes());
        }

        // Update labels
        if (command.labels() != null) {
            ci.updateLabels(command.labels());
        }

        // Update owner
        if (command.owner() != null) {
            ci.updateOwner(command.owner());
        }

        // Update display name
        if (command.displayName() != null) {
            ci.updateDisplayName(command.displayName());
        }

        // Save aggregate
        CI savedCI = ciRepository.save(ci);

        // Publish domain events
        publishDomainEvents(savedCI);

        log.info("Updated CI {}", ciId.getValue());
        return ciMapper.toResponse(savedCI);
    }

    /**
     * Changes the CI status.
     */
    public CIResponse changeStatus(CIId ciId, CILifecycleStatus newStatus, String reason) {
        log.info("Changing status of CI {} to {}", ciId.getValue(), newStatus);

        CI ci = ciRepository.findById(ciId)
            .orElseThrow(() -> new CINotFoundException("CI not found: " + ciId));

        ci.changeStatus(newStatus, reason);

        CI savedCI = ciRepository.save(ci);
        publishDomainEvents(savedCI);

        log.info("Changed status of CI {} to {}", ciId.getValue(), newStatus);
        return ciMapper.toResponse(savedCI);
    }

    /**
     * Puts a CI into maintenance mode.
     */
    public CIResponse enterMaintenance(CIId ciId, String reason) {
        return changeStatus(ciId, CILifecycleStatus.MAINTENANCE, reason);
    }

    /**
     * Returns a CI to active status.
     */
    public CIResponse returnToActive(CIId ciId, String reason) {
        return changeStatus(ciId, CILifecycleStatus.ACTIVE, reason);
    }

    /**
     * Decommission a CI.
     */
    public CIResponse decommission(CIId ciId, String reason) {
        log.info("Decommissioning CI {}", ciId.getValue());

        CI ci = ciRepository.findById(ciId)
            .orElseThrow(() -> new CINotFoundException("CI not found: " + ciId));

        ci.decommission(reason);

        CI savedCI = ciRepository.save(ci);
        publishDomainEvents(savedCI);

        log.info("Decommissioned CI {}", ciId.getValue());
        return ciMapper.toResponse(savedCI);
    }

    /**
     * Gets a CI by ID.
     */
    @Transactional(readOnly = true)
    public Optional<CIResponse> getCI(CIId ciId) {
        return ciRepository.findById(ciId)
            .map(ciMapper::toResponse);
    }

    /**
     * Gets all CIs.
     */
    @Transactional(readOnly = true)
    public List<CIResponse> getAllCIs() {
        return ciRepository.findAll().stream()
            .map(ciMapper::toResponse)
            .toList();
    }

    /**
     * Gets CIs by type with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CIResponse> getCIsByType(CITypeId typeId, Pageable pageable) {
        return ciRepository.findByTypeId(typeId, pageable)
            .map(ciMapper::toResponse);
    }

    /**
     * Gets CIs by status with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CIResponse> getCIsByStatus(CILifecycleStatus status, Pageable pageable) {
        return ciRepository.findByStatus(status, pageable)
            .map(ciMapper::toResponse);
    }

    /**
     * Searches CIs by name.
     */
    @Transactional(readOnly = true)
    public Page<CIResponse> searchCIs(String searchTerm, Pageable pageable) {
        return ciRepository.searchByName(searchTerm, pageable)
            .map(ciMapper::toResponse);
    }

    /**
     * Finds CIs by filters.
     */
    @Transactional(readOnly = true)
    public Page<CIResponse> findByFilters(CITypeId typeId, CILifecycleStatus status, 
                                          String owner, String environment, Pageable pageable) {
        return ciRepository.findByFilters(typeId, status, owner, environment, pageable)
            .map(ciMapper::toResponse);
    }

    /**
     * Gets multiple CIs by IDs.
     */
    @Transactional(readOnly = true)
    public List<CIResponse> getCIsByIds(Collection<CIId> ids) {
        return ciRepository.findByIdIn(ids).stream()
            .map(ciMapper::toResponse)
            .toList();
    }

    /**
     * Gets the correlation labels for a CI.
     */
    @Transactional(readOnly = true)
    public LabelSet getCorrelationLabels(CIId ciId) {
        CI ci = ciRepository.findById(ciId)
            .orElseThrow(() -> new CINotFoundException("CI not found: " + ciId));
        return ci.getCorrelationLabels();
    }

    /**
     * Deletes a CI.
     */
    public void deleteCI(CIId ciId) {
        log.info("Deleting CI {}", ciId.getValue());

        if (!ciRepository.findById(ciId).isPresent()) {
            throw new CINotFoundException("CI not found: " + ciId);
        }

        ciRepository.deleteById(ciId);
        log.info("Deleted CI {}", ciId.getValue());
    }

    /**
     * Publishes domain events to the event bus.
     */
    private void publishDomainEvents(CI ci) {
        for (DomainEvent event : ci.getDomainEvents()) {
            eventPublisher.publishEvent(event);
        }
        ci.clearDomainEvents();
    }

    // Exception classes
    public static class CINotFoundException extends RuntimeException {
        public CINotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateCIException extends RuntimeException {
        public DuplicateCIException(String message) {
            super(message);
        }
    }
}
