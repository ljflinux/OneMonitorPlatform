package io.onemonitor.cmdb.domain.model.identifier;

import io.onemonitor.common.domain.base.Identifier;

/**
 * Identifier for a Configuration Item (CI).
 */
public final class CIId extends Identifier.AbstractIdentifier {

    public CIId() {
        super(java.util.UUID.randomUUID().toString());
    }

    public CIId(String value) {
        super(value);
    }

    @Override
    protected void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CI ID cannot be null or blank");
        }
    }

    public static CIId fromString(String value) {
        return new CIId(value);
    }

    public static CIId generate() {
        return new CIId();
    }
}
