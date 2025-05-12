package org.smaskee.blockFaker.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.structs.FakeEntity;

import java.util.logging.Level;

/**
 * Handles packet operations for fake blocks.
 */
public class BlockPacketHandler implements PacketHandler {
    private final BlockFaker plugin;
    private final ProtocolManager protocolManager;

    public BlockPacketHandler(BlockFaker plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void sendPacket(Player player, FakeEntity entity, boolean sendFake) {
        if (!(entity instanceof FakeBlock)) {
            return;
        }

        FakeBlock block = (FakeBlock) entity;
        Material material = block.getMaterial(player.getUniqueId());
        
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
            BlockPosition position = new BlockPosition(
                block.getLocation().getBlockX(),
                block.getLocation().getBlockY(),
                block.getLocation().getBlockZ()
            );
            packet.getBlockPositionModifier().write(0, position);
            packet.getBlockData().write(0, WrappedBlockData.createData(material));
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to send block packet", e);
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
        if (!event.isIncoming() || event.getPacketType() != PacketType.Play.Client.BLOCK_DIG) {
            return;
        }

        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();
        
        // Handle block interaction
        // TODO: Implement block interaction handling
    }
} 