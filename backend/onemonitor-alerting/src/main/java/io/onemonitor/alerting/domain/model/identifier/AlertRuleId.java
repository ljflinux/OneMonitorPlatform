package io.onemonitor.alerting.domain.model.identifier;

import io.onemonitor.common.domain.base.Identifier;

/**
 * Identifier for an Alert Rule.
 */
public final class AlertRuleId extends Identifier.AbstractIdentifier {

    public AlertRuleId() {
        super(java.util.UUID.randomUUID().toString());
    }

    public AlertRuleId(String value) {
        super(value);
    }

    @Override
    protected void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Alert Rule ID cannot be null or blank");
        }
    }

    public static AlertRuleId fromString(String value) {
        return new AlertRuleId(value);
    }

    public static AlertRuleId generate() {
        return new AlertRuleId();
    }
}
