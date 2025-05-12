package org.smaskee.blockFaker.structs;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.managers.PacketHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract base class for all fake entities.
 * Provides common functionality and state management.
 */
public abstract class AbstractFakeEntity implements FakeEntity {
    protected final String name;
    protected final Location location;
    protected final Set<UUID> visibleTo;
    protected boolean clickable;
    protected final PacketHandler packetHandler;
    protected final BlockFaker plugin;
    protected final EntityType type;

    protected AbstractFakeEntity(String name, Location location, EntityType type, BlockFaker plugin, PacketHandler packetHandler) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.plugin = plugin;
        this.packetHandler = packetHandler;
        this.visibleTo = new HashSet<>();
        this.clickable = true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Set<UUID> getVisibleTo() {
        return new HashSet<>(visibleTo);
    }

    @Override
    public boolean isVisibleTo(UUID playerId) {
        return visibleTo.contains(playerId);
    }

    @Override
    public void setVisibleTo(UUID playerId, boolean visible) {
        if (visible) {
            visibleTo.add(playerId);
        } else {
            visibleTo.remove(playerId);
        }
    }

    @Override
    public boolean isClickable() {
        return clickable;
    }

    @Override
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    @Override
    public void sendToPlayer(Player player, boolean sendFake) {
        if (sendFake) {
            packetHandler.sendPacket(player, this, true);
        } else {
            packetHandler.sendPacket(player, this, false);
        }
    }

    @Override
    public void removeFromPlayer(Player player) {
        packetHandler.sendPacket(player, this, false);
    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public int getPriority() {
        return type.getPriority();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractFakeEntity that = (AbstractFakeEntity) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
} 