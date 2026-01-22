package io.onemonitor.monitoring.application.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onemonitor.monitoring.application.dto.TargetResponse;
import io.onemonitor.monitoring.domain.model.Target;
import org.springframework.stereotype.Component;

/**
 * Mapper for Target domain model to DTO conversion.
 */
@Component
public class TargetMapper {

    private final ObjectMapper objectMapper;

    public TargetMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a Target domain model to a TargetResponse DTO.
     */
    public TargetResponse toResponse(Target target) {
        if (target == null) {
            return null;
        }

        String labelsJson = null;
        if (target.getStaticLabels() != null && !target.getStaticLabels().isEmpty()) {
            try {
                labelsJson = objectMapper.writeValueAsString(target.getStaticLabels());
            } catch (JsonProcessingException e) {
                // Ignore - labelsJson will be null
            }
        }

        return new TargetResponse(
            target.getId().getValue(),
            target.getName(),
            target.getDescription(),
            target.getType().name(),
            target.getType().getDisplayName(),
            target.getEndpoint(),
            target.getPort(),
            target.getStatus().name(),
            target.getStatus().getDisplayName(),
            target.getCiId(),
            labelsJson,
            target.getLastScrapeSuccess(),
            target.getLastScrapeFailure(),
            target.getLastScrapeError(),
            target.getNextScrape(),
            target.getScrapeInterval(),
            target.getScrapeTimeout(),
            target.isEnabled(),
            target.getCreatedAt(),
            target.getUpdatedAt()
        );
    }
}
