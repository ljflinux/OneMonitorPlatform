package io.onemonitor.monitoring.domain.model.identifier;

import io.onemonitor.common.domain.base.Identifier;

/**
 * Identifier for a Monitoring Target.
 */
public final class TargetId extends Identifier.AbstractIdentifier {

    public TargetId() {
        super(java.util.UUID.randomUUID().toString());
    }

    public TargetId(String value) {
        super(value);
    }

    @Override
    protected void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Target ID cannot be null or blank");
        }
    }

    public static TargetId fromString(String value) {
        return new TargetId(value);
    }

    public static TargetId generate() {
        return new TargetId();
    }
}
