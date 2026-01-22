package io.onemonitor.alerting.domain.model.identifier;

import io.onemonitor.common.domain.base.Identifier;

/**
 * Identifier for an Alert Instance (active alert).
 */
public final class AlertInstanceId extends Identifier.AbstractIdentifier {

    public AlertInstanceId() {
        super(java.util.UUID.randomUUID().toString());
    }

    public AlertInstanceId(String value) {
        super(value);
    }

    @Override
    protected void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Alert Instance ID cannot be null or blank");
        }
    }

    public static AlertInstanceId fromString(String value) {
        return new AlertInstanceId(value);
    }

    public static AlertInstanceId generate() {
        return new AlertInstanceId();
    }
}
