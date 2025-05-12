package org.smaskee.blockFaker.managers;

import org.bukkit.Location;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.structs.FakeEntity;
import org.smaskee.blockFaker.structs.FakeSkull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages entities at specific locations and handles conflicts between them.
 * Enforces one block and one skull per location, with mutual exclusivity of visibility.
 */
public class LocationManager {
    private final BlockFaker plugin;
    // Maps location to the block at that location (null if none)
    private final Map<Location, FakeBlock> blocks;
    // Maps location to the skull at that location (null if none)
    private final Map<Location, FakeSkull> skulls;
    // Maps location to which type is currently visible (null if none)
    private final Map<Location, EntityType> visibleType;
    // Maps location to player-specific visibility overrides
    private final Map<Location, Map<UUID, EntityType>> playerVisibleType;

    public LocationManager(BlockFaker plugin) {
        this.plugin = plugin;
        this.blocks = new ConcurrentHashMap<>();
        this.skulls = new ConcurrentHashMap<>();
        this.visibleType = new ConcurrentHashMap<>();
        this.playerVisibleType = new ConcurrentHashMap<>();
    }

    /**
     * Registers an entity at its location.
     * @param entity The entity to register
     * @return true if registration was successful, false if an entity of the same type already exists
     */
    public boolean registerEntity(FakeEntity entity) {
        Location loc = entity.getLocation();
        
        if (entity instanceof FakeBlock) {
            if (blocks.containsKey(loc)) {
                return false; // Block already exists at this location
            }
            blocks.put(loc, (FakeBlock) entity);
        } else if (entity instanceof FakeSkull) {
            if (skulls.containsKey(loc)) {
                return false; // Skull already exists at this location
            }
            skulls.put(loc, (FakeSkull) entity);
        }
        
        return true;
    }

    /**
     * Unregisters an entity from its location.
     * @param entity The entity to unregister
     */
    public void unregisterEntity(FakeEntity entity) {
        Location loc = entity.getLocation();
        
        if (entity instanceof FakeBlock) {
            blocks.remove(loc);
        } else if (entity instanceof FakeSkull) {
            skulls.remove(loc);
        }
        
        // Clean up visibility tracking if no entities remain
        if (!blocks.containsKey(loc) && !skulls.containsKey(loc)) {
            visibleType.remove(loc);
            playerVisibleType.remove(loc);
        }
    }

    /**
     * Gets the block at a location.
     * @param location The location to check
     * @return The block at the location, or null if none exists
     */
    public FakeBlock getBlock(Location location) {
        return blocks.get(location);
    }

    /**
     * Gets the skull at a location.
     * @param location The location to check
     * @return The skull at the location, or null if none exists
     */
    public FakeSkull getSkull(Location location) {
        return skulls.get(location);
    }

    /**
     * Gets the currently visible entity type at a location for a specific player.
     * @param location The location to check
     * @param playerId The UUID of the player
     * @return The visible entity type, or null if none is visible
     */
    public EntityType getVisibleType(Location location, UUID playerId) {
        Map<UUID, EntityType> playerTypes = playerVisibleType.get(location);
        if (playerTypes != null && playerTypes.containsKey(playerId)) {
            return playerTypes.get(playerId);
        }
        return visibleType.get(location);
    }

    /**
     * Sets the visible entity type at a location for a specific player.
     * @param location The location to update
     * @param playerId The UUID of the player
     * @param type The entity type to make visible
     */
    public void setVisibleType(Location location, UUID playerId, EntityType type) {
        if (type == null) {
            clearVisibleType(location, playerId);
            return;
        }

        // Update global visibility if no player-specific override
        if (playerId == null) {
            visibleType.put(location, type);
            return;
        }

        // Update player-specific visibility
        playerVisibleType.computeIfAbsent(location, k -> new ConcurrentHashMap<>())
                .put(playerId, type);
    }

    /**
     * Clears the visible entity type at a location for a specific player.
     * @param location The location to update
     * @param playerId The UUID of the player
     */
    public void clearVisibleType(Location location, UUID playerId) {
        if (playerId == null) {
            visibleType.remove(location);
            return;
        }

        Map<UUID, EntityType> playerTypes = playerVisibleType.get(location);
        if (playerTypes != null) {
            playerTypes.remove(playerId);
            if (playerTypes.isEmpty()) {
                playerVisibleType.remove(location);
            }
        }
    }

    /**
     * Gets all locations that have registered entities.
     * @return Set of locations with entities
     */
    public Set<Location> getRegisteredLocations() {
        Set<Location> locations = new HashSet<>();
        locations.addAll(blocks.keySet());
        locations.addAll(skulls.keySet());
        return locations;
    }

    /**
     * Checks if a location has both a block and a skull.
     * @param location The location to check
     * @return true if both a block and skull exist at the location
     */
    public boolean hasBothTypes(Location location) {
        return blocks.containsKey(location) && skulls.containsKey(location);
    }

    /**
     * Cleans up all data for a specific player.
     * @param playerId The UUID of the player
     */
    public void cleanupPlayer(UUID playerId) {
        for (Map<UUID, EntityType> playerTypes : playerVisibleType.values()) {
            playerTypes.remove(playerId);
        }
        playerVisibleType.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Checks if any entity exists at a location.
     * @param location The location to check
     * @return true if any entity exists at the location
     */
    public boolean hasEntity(Location location) {
        return blocks.containsKey(location) || skulls.containsKey(location);
    }
} 