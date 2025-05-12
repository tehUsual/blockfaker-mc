package org.smaskee.blockFaker.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.structs.FakeEntity;
import org.smaskee.blockFaker.structs.FakeSkull;
import org.smaskee.blockFaker.managers.PacketHandler;

import java.util.logging.Level;

/**
 * Handles packet operations for fake skulls.
 */
public class SkullPacketHandler implements PacketHandler {
    private final BlockFaker plugin;
    private final ProtocolManager protocolManager;

    public SkullPacketHandler(BlockFaker plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void sendPacket(Player player, FakeEntity entity, boolean sendFake) {
        if (!(entity instanceof FakeSkull)) {
            return;
        }
        FakeSkull skull = (FakeSkull) entity;
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
            BlockPosition position = new BlockPosition(
                skull.getLocation().getBlockX(),
                skull.getLocation().getBlockY(),
                skull.getLocation().getBlockZ()
            );
            packet.getBlockPositionModifier().write(0, position);
            if (sendFake) {
                // Show the skull (as a player head block)
                packet.getBlockData().write(0, WrappedBlockData.createData(Material.PLAYER_HEAD));
            } else {
                // Hide the skull (show air)
                packet.getBlockData().write(0, WrappedBlockData.createData(Material.AIR));
            }
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to send skull packet", e);
        }
    }

    @Override
    public void sendMultiplePackets(Player player, FakeEntity[] entities, boolean sendFake) {
        for (FakeEntity entity : entities) {
            sendPacket(player, entity, sendFake);
        }
    }

    @Override
    public void handlePacketEvent(org.smaskee.blockFaker.managers.PacketEvent event) {
        // Handle incoming block dig packets to prevent breaking fake skulls
        if (event.isIncoming() && event.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
            // TODO: Implement skull-specific packet event handling if needed
        }
    }
} 