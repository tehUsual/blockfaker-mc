package org.smaskee.blockFaker.managers;

import org.bukkit.entity.Player;
import org.smaskee.blockFaker.structs.FakeEntity;

/**
 * Interface for handling packet operations for fake entities.
 */
public interface PacketHandler {
    /**
     * Sends a packet to a player for a specific entity.
     * @param player The player to send the packet to
     * @param entity The entity to send
     * @param sendFake Whether to send the fake version or real version
     */
    void sendPacket(Player player, FakeEntity entity, boolean sendFake);

    /**
     * Sends multiple packets to a player for multiple entities.
     * @param player The player to send the packets to
     * @param entities The entities to send
     * @param sendFake Whether to send the fake versions or real versions
     */
    void sendMultiplePackets(Player player, FakeEntity[] entities, boolean sendFake);

    /**
     * Handles a packet event.
     * @param event The packet event to handle
     */
    void handlePacketEvent(org.smaskee.blockFaker.managers.PacketEvent event);
} 