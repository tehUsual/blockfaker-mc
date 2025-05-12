package org.smaskee.blockFaker.structs;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.managers.PacketHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a fake skull in the world.
 */
public class FakeSkull extends AbstractFakeEntity {
    private final String textureName;
    private final BlockFace rotation;
    private final boolean isWallSkull;
    private final Set<UUID> visibleTo;

    public FakeSkull(String name, Location location, String textureName, BlockFace rotation, boolean isWallSkull,
                    BlockFaker plugin, PacketHandler packetHandler) {
        super(name, location, EntityType.SKULL, plugin, packetHandler);
        this.textureName = textureName;
        this.rotation = rotation;
        this.isWallSkull = isWallSkull;
        this.visibleTo = new HashSet<>();
    }

    public FakeSkull(String name, Location location, String textureName, BlockFace rotation) {
        this(name, location, textureName, rotation, false, null, null);
    }

    public FakeSkull(String name, Location location, String textureName, boolean isWallSkull) {
        this(name, location, textureName, BlockFace.SOUTH, isWallSkull, null, null);
    }

    /**
     * Gets the texture name of this skull.
     * @return The texture name
     */
    public String getTextureName() {
        return textureName;
    }

    /**
     * Gets the rotation of this skull.
     * @return The rotation
     */
    public BlockFace getRotation() {
        return rotation;
    }

    /**
     * Checks if this is a wall skull.
     * @return true if this is a wall skull
     */
    public boolean isWallSkull() {
        return isWallSkull;
    }

    public Set<UUID> getVisibleTo() {
        return visibleTo;
    }

    public boolean isVisibleTo(UUID playerId) {
        return visibleTo.contains(playerId);
    }

    public void setVisibleTo(UUID playerId, boolean visible) {
        if (visible) {
            visibleTo.add(playerId);
        } else {
            visibleTo.remove(playerId);
        }
    }

    @Override
    public String toString() {
        return String.format("FakeSkull{name='%s', location=%s, texture='%s', rotation=%s, isWallSkull=%s}",
                name, location, textureName, rotation, isWallSkull);
    }
}
