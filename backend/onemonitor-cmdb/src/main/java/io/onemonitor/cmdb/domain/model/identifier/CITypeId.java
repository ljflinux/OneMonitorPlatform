package io.onemonitor.cmdb.domain.model.identifier;

import io.onemonitor.common.domain.base.Identifier;

/**
 * Identifier for a CI Type definition.
 */
public final class CITypeId extends Identifier.AbstractIdentifier {

    public CITypeId() {
        super(java.util.UUID.randomUUID().toString());
    }

    public CITypeId(String value) {
        super(value);
    }

    @Override
    protected void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CI Type ID cannot be null or blank");
        }
    }

    public static CITypeId fromString(String value) {
        return new CITypeId(value);
    }

    public static CITypeId generate() {
        return new CITypeId();
    }
}
