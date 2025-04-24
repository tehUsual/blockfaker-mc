package org.smaskee.blockFaker.structs;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FakeBlock {
    private final String name;
    private final Location location;
    private final Material material;
    private final Set<UUID> visibleTo;

    public FakeBlock(String name, Location location, Material material) {
        this.name = name;
        this.location = location;
        this.material = material;
        this.visibleTo = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
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
