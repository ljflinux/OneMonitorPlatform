package io.onemonitor.cmdb.domain.model;

/**
 * Enumeration of CI lifecycle status values.
 * 
 * State machine transitions:
 * - ACTIVE -> MAINTENANCE, DECOMMISSIONED
 * - MAINTENANCE -> ACTIVE, DECOMMISSIONED
 * - DECOMMISSIONED -> DISPOSED (final state)
 * - DISPOSED (final state)
 */
public enum CILifecycleStatus {
    ACTIVE("Active", "CI is in normal operation"),
    MAINTENANCE("Maintenance", "CI is under maintenance"),
    DECOMMISSIONED("Decommissioned", "CI has been retired from service"),
    DISPOSED("Disposed", "CI data has been archived/purged");

    private final String displayName;
    private final String description;

    CILifecycleStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns true if this status is a terminal state.
     */
    public boolean isTerminal() {
        return this == DECOMMISSIONED || this == DISPOSED;
    }

    /**
     * Returns true if this status allows updates.
     */
    public boolean allowsUpdates() {
        return this == ACTIVE || this == MAINTENANCE;
    }

    /**
     * Returns true if this status transition is valid.
     */
    public boolean canTransitionTo(CILifecycleStatus target) {
        return switch (this) {
            case ACTIVE -> target == MAINTENANCE || target == DECOMMISSIONED;
            case MAINTENANCE -> target == ACTIVE || target == DECOMMISSIONED;
            case DECOMMISSIONED -> target == DISPOSED;
            case DISPOSED -> false; // Terminal state
        };
    }
}
