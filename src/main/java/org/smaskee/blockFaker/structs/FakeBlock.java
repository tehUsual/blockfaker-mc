package org.smaskee.blockFaker.structs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.managers.PacketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a fake block in the world.
 */
public class FakeBlock extends AbstractFakeEntity {
    private final Material baseMaterial;
    private final Map<UUID, Material> materialOverrides;

    public FakeBlock(String name, Location location, Material material, BlockFaker plugin, PacketHandler packetHandler) {
        super(name, location, EntityType.BLOCK, plugin, packetHandler);
        this.baseMaterial = material;
        this.materialOverrides = new HashMap<>();
    }

    /**
     * Gets the base material of this block.
     * @return The base material
     */
    public Material getBaseMaterial() {
        return baseMaterial;
    }

    /**
     * Gets the material that should be shown to a specific player.
     * @param playerId The UUID of the player
     * @return The material to show
     */
    public Material getMaterial(UUID playerId) {
        return materialOverrides.getOrDefault(playerId, baseMaterial);
    }

    /**
     * Sets a material override for a specific player.
     * @param playerId The UUID of the player
     * @param material The material to override with
     */
    public void setMaterialOverride(UUID playerId, Material material) {
        materialOverrides.put(playerId, material);
    }

    /**
     * Clears the material override for a specific player.
     * @param playerId The UUID of the player
     */
    public void clearMaterialOverride(UUID playerId) {
        materialOverrides.remove(playerId);
    }

    /**
     * Checks if a player has a material override.
     * @param playerId The UUID of the player
     * @return true if the player has a material override
     */
    public boolean hasMaterialOverride(UUID playerId) {
        return materialOverrides.containsKey(playerId);
    }

    @Override
    public String toString() {
        return String.format("FakeBlock{name='%s', location=%s, material=%s}", name, location, baseMaterial);
    }
}
