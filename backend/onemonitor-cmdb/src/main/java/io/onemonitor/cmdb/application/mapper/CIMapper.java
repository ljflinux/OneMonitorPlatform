package io.onemonitor.cmdb.application.mapper;

import io.onemonitor.cmdb.application.dto.CIResponse;
import io.onemonitor.cmdb.domain.model.CI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper for CI domain model to DTO conversion.
 */
@Component
public class CIMapper {

    /**
     * Converts a CI domain model to a CIResponse DTO.
     */
    public CIResponse toResponse(CI ci) {
        if (ci == null) {
            return null;
        }

        Map<String, String> labelMap = new HashMap<>();
        if (ci.getLabels() != null) {
            labelMap = ci.getLabels().asMap();
        }

        return new CIResponse(
            ci.getId().getValue(),
            ci.getTypeId().getValue(),
            ci.getName(),
            ci.getDisplayName(),
            ci.getStatus().name(),
            ci.getStatus().getDisplayName(),
            ci.getAttributes(),
            labelMap,
            ci.getOwner(),
            ci.getDepartment(),
            ci.getLocation(),
            ci.getEnvironment(),
            ci.getDescription(),
            ci.getCreatedBy(),
            ci.getUpdatedBy(),
            ci.getCreatedAt(),
            ci.getUpdatedAt()
        );
    }
}
