package io.onemonitor.cmdb.domain.model.identifier;

import io.onemonitor.common.domain.base.Identifier;

/**
 * Identifier for a CI Relation.
 */
public final class CIRelationId extends Identifier.AbstractIdentifier {

    public CIRelationId() {
        super(java.util.UUID.randomUUID().toString());
    }

    public CIRelationId(String value) {
        super(value);
    }

    @Override
    protected void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CI Relation ID cannot be null or blank");
        }
    }

    public static CIRelationId fromString(String value) {
        return new CIRelationId(value);
    }

    public static CIRelationId generate() {
        return new CIRelationId();
    }
}
