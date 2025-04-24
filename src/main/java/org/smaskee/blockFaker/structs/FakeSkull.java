package org.smaskee.blockFaker.structs;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FakeSkull {
    private final String name;
    private final Location location;
    private final BlockFace rotation;
    private final boolean isWallSkull;
    private final String textureName;
    private final Set<UUID> visibleTo;

    public FakeSkull(String name, Location location, String textureName, BlockFace rotation, boolean isWallSkull) {
        this.name = name;
        this.location = location;
        this.rotation = rotation;
        this.isWallSkull = isWallSkull;
        this.textureName = textureName;
        this.visibleTo = new HashSet<>();
    }

    public FakeSkull(String name, Location location, String textureName) {
        this(name, location, textureName, BlockFace.SOUTH, false);
    }

    public FakeSkull(String name, Location location, String textureName, BlockFace rotation) {
        this(name, location, textureName, rotation, false);
    }

    public FakeSkull(String name, Location location, String textureName, boolean isWallSkull) {
        this(name, location, textureName, BlockFace.SOUTH, isWallSkull);
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public String getTextureName() {
        return textureName;
    }

    public BlockFace getRotation() {
        return rotation;
    }

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
}
