package org.smaskee.blockFaker.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

/**
 * Class representing a packet event in the system.
 */
public class PacketEvent {
    private final Player player;
    private final PacketContainer packet;
    private final boolean isIncoming;
    private boolean cancelled;

    public PacketEvent(Player player, PacketContainer packet, boolean isIncoming) {
        this.player = player;
        this.packet = packet;
        this.isIncoming = isIncoming;
        this.cancelled = false;
    }

    /**
     * Gets the player involved in this packet event.
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the packet involved in this event.
     * @return The packet
     */
    public PacketContainer getPacket() {
        return packet;
    }

    /**
     * Gets the type of this packet.
     * @return The packet type
     */
    public PacketType getPacketType() {
        return packet.getType();
    }

    /**
     * Checks if this is an incoming packet.
     * @return true if the packet is incoming, false if outgoing
     */
    public boolean isIncoming() {
        return isIncoming;
    }

    /**
     * Checks if this event has been cancelled.
     * @return true if the event is cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether this event should be cancelled.
     * @param cancelled Whether to cancel the event
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
} 