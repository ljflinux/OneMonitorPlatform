package io.onemonitor.alerting.application.mapper;

import io.onemonitor.alerting.application.dto.AlertInstanceResponse;
import io.onemonitor.alerting.application.dto.AlertRuleResponse;
import io.onemonitor.alerting.domain.model.AlertInstance;
import io.onemonitor.alerting.domain.model.AlertRule;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for Alerting domain models to DTO conversion.
 */
@Component
public class AlertingMapper {

    /**
     * Converts an AlertRule domain model to an AlertRuleResponse DTO.
     */
    public AlertRuleResponse toAlertRuleResponse(AlertRule rule) {
        if (rule == null) {
            return null;
        }

        Map<String, String> labelMap = rule.getLabels() != null 
            ? new HashMap<>(rule.getLabels()) 
            : new HashMap<>();

        List<String> notificationChannels = parseNotificationChannels(
            rule.getNotificationChannelsJson());

        return new AlertRuleResponse(
            rule.getId().getValue(),
            rule.getName(),
            rule.getDisplayName(),
            rule.getDescription(),
            rule.getSeverity().name(),
            rule.getSeverity().getDisplayName(),
            rule.getPromqlExpression(),
            rule.getEvaluationInterval(),
            rule.getForDuration(),
            rule.getSummaryTemplate(),
            rule.getDescriptionTemplate(),
            rule.getRunbookUrl(),
            labelMap,
            rule.getCiId(),
            notificationChannels,
            rule.isEnabled(),
            rule.isAutoResolve(),
            rule.getResolveTimeout(),
            rule.getFireCount(),
            rule.getLastFiredAt(),
            rule.getCreatedAt(),
            rule.getUpdatedAt(),
            rule.getCreatedBy()
        );
    }

    /**
     * Converts an AlertInstance domain model to an AlertInstanceResponse DTO.
     */
    public AlertInstanceResponse toAlertInstanceResponse(AlertInstance instance) {
        if (instance == null) {
            return null;
        }

        Map<String, String> labelMap = parseLabels(instance.getLabelsJson());
        Map<String, Object> valueMap = parseValues(instance.getValuesJson());

        return new AlertInstanceResponse(
            instance.getId().getValue(),
            instance.getRuleId().getValue(),
            instance.getRuleName(),
            instance.getFingerprint(),
            instance.getStatus().name(),
            instance.getStatus().getName(),
            instance.getSeverity().name(),
            instance.getSeverity().getDisplayName(),
            instance.getCiId(),
            labelMap,
            valueMap,
            instance.getCurrentValue(),
            instance.getSummary(),
            instance.getDescription(),
            instance.getRunbookUrl(),
            instance.getFireCount(),
            instance.getFiredAt(),
            instance.getUpdatedAt(),
            instance.getAcknowledgedAt(),
            instance.getAcknowledgedBy(),
            instance.getAckComment(),
            instance.getResolvedAt(),
            instance.getResolvedBy(),
            instance.getResolveComment(),
            instance.isAutoResolved(),
            instance.getNotificationCount(),
            instance.getDuration()
        );
    }

    private List<String> parseNotificationChannels(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            // Simple parsing of the toString() output
            String cleaned = json.replace("[", "").replace("]", "");
            if (cleaned.isBlank()) {
                return List.of();
            }
            return List.of(cleaned.split(", "));
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, String> parseLabels(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null || json.isBlank()) {
            return result;
        }
        try {
            // Parse {key1=value1, key2=value2} format
            String cleaned = json.substring(1, json.length() - 1);
            if (cleaned.isBlank()) {
                return result;
            }
            String[] pairs = cleaned.split(", ");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    result.put(kv[0].trim(), kv[1].trim());
                }
            }
        } catch (Exception e) {
            // Return empty map on parse error
        }
        return result;
    }

    private Map<String, Object> parseValues(String json) {
        Map<String, Object> result = new HashMap<>();
        if (json == null || json.isBlank()) {
            return result;
        }
        try {
            // Parse {key1=value1, key2=value2} format
            String cleaned = json.substring(1, json.length() - 1);
            if (cleaned.isBlank()) {
                return result;
            }
            String[] pairs = cleaned.split(", ");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    String value = kv[1].trim();
                    try {
                        if (value.contains(".")) {
                            result.put(kv[0].trim(), Double.parseDouble(value));
                        } else {
                            result.put(kv[0].trim(), Long.parseLong(value));
                        }
                    } catch (NumberFormatException e) {
                        result.put(kv[0].trim(), value);
                    }
                }
            }
        } catch (Exception e) {
            // Return empty map on parse error
        }
        return result;
    }
}
