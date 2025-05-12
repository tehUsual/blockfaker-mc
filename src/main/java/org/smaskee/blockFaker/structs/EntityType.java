package org.smaskee.blockFaker.structs;

/**
 * Enum representing the different types of fake entities.
 */
public enum EntityType {
    BLOCK(1),
    SKULL(2);

    private final int priority;

    EntityType(int priority) {
        this.priority = priority;
    }

    /**
     * Gets the priority of this entity type.
     * Higher priority entities will override lower priority ones at the same location.
     * @return The priority value
     */
    public int getPriority() {
        return priority;
    }
} 