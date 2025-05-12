package org.smaskee.blockFaker.structs;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Interface representing a fake entity in the world.
 * This is the base interface for all fake entities (blocks, skulls, etc.).
 */
public interface FakeEntity {
    /**
     * Gets the name of this entity.
     * @return The entity's name
     */
    String getName();

    /**
     * Gets the location of this entity.
     * @return The entity's location
     */
    Location getLocation();

    /**
     * Gets the set of player UUIDs who can see this entity.
     * @return Set of player UUIDs
     */
    Set<UUID> getVisibleTo();

    /**
     * Checks if this entity is visible to a specific player.
     * @param playerId The UUID of the player to check
     * @return true if the player can see this entity
     */
    boolean isVisibleTo(UUID playerId);

    /**
     * Sets the visibility of this entity for a specific player.
     * @param playerId The UUID of the player
     * @param visible Whether the entity should be visible
     */
    void setVisibleTo(UUID playerId, boolean visible);

    /**
     * Checks if this entity is clickable.
     * @return true if the entity is clickable
     */
    boolean isClickable();

    /**
     * Sets whether this entity is clickable.
     * @param clickable Whether the entity should be clickable
     */
    void setClickable(boolean clickable);

    /**
     * Sends this entity to a player.
     * @param player The player to send the entity to
     * @param sendFake Whether to send the fake version or real version
     */
    void sendToPlayer(Player player, boolean sendFake);

    /**
     * Removes this entity from a player's view.
     * @param player The player to remove the entity from
     */
    void removeFromPlayer(Player player);

    /**
     * Gets the type of this entity.
     * @return The entity type
     */
    EntityType getType();

    /**
     * Gets the priority of this entity.
     * Higher priority entities will override lower priority ones at the same location.
     * @return The entity's priority
     */
    int getPriority();
} 