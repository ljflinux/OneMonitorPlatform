package io.onemonitor.common.domain.base;

/**
 * Base interface for all entities in the domain.
 * 
 * An entity has a unique identity that persists beyond its attribute values.
 */
public interface Entity<ID extends Identifier> {

    /**
     * Returns the unique identifier of this entity.
     */
    ID getId();

    /**
     * Returns true if this entity is the same as the other entity.
     * Two entities are equal if they have the same identity.
     */
    default boolean sameIdentityAs(Entity<ID> other) {
        if (other == null) {
            return false;
        }
        return this.getId().equals(other.getId());
    }

    /**
     * Checks if this entity has been deleted (soft delete).
     */
    default boolean isDeleted() {
        return false;
    }
}
