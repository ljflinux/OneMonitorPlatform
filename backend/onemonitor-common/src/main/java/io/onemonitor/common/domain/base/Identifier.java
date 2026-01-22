package io.onemonitor.common.domain.base;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base interface for all identifiers in the domain.
 * 
 * An identifier uniquely identifies an entity within the domain.
 */
public interface Identifier extends Serializable {

    /**
     * Returns the value of this identifier.
     */
    String getValue();

    /**
     * Returns true if this identifier is equal to the other identifier.
     */
    @Override
    boolean equals(Object o);

    /**
     * Returns the hash code of this identifier.
     */
    @Override
    int hashCode();

    /**
     * Returns the string representation of this identifier.
     */
    @Override
    String toString();

    /**
     * Creates a new identifier from the given value.
     */
    static <T extends Identifier> T fromString(String value, Factory<T> factory) {
        return factory.create(value);
    }

    /**
     * Functional interface for creating identifiers.
     */
    @FunctionalInterface
    interface Factory<T extends Identifier> {
        T create(String value);
    }

    /**
     * Abstract base implementation for string-based identifiers.
     */
    abstract class AbstractIdentifier implements Identifier {
        private final String value;

        protected AbstractIdentifier(String value) {
            this.value = Objects.requireNonNull(value, "Identifier value cannot be null");
            validateValue(value);
        }

        protected void validateValue(String value) {
            // Default implementation - can be overridden
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AbstractIdentifier that = (AbstractIdentifier) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
