package org.smaskee.blockFaker.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.managers.VisibilityManager;

import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.structs.FakeSkull;

import java.lang.reflect.Field;

public class BlockInteractionPacketListener {
    private final Plugin plugin;
    private final ProtocolManager protocolManager;
    private final VisibilityManager visibilityManager;

    public BlockInteractionPacketListener(BlockFaker plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.visibilityManager = plugin.getVisibilityManager();

        registerPacketListeners();
    }


    private void registerPacketListeners() {

//        // Cancel clicking on fake blocks
//        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
//                PacketType.Play.Client.USE_ITEM) {
//            @Override
//            public void onPacketReceiving(PacketEvent event) {
//                if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
//                    Player player = event.getPlayer();
//                    PacketContainer packet = event.getPacket();
//
//                    try {
//                        Object handle = packet.getHandle();
//
//                        if (handle instanceof ServerboundUseItemOnPacket nmsPacket) {
//                            net.minecraft.world.phys.BlockHitResult hit = nmsPacket.getHitResult();
//                            BlockPos nmsBlockPos = hit.getBlockPos();
//
//                            Location loc = new Location(player.getWorld(), nmsBlockPos.getX(), nmsBlockPos.getY(), nmsBlockPos.getZ());
//                            String strPos = String.format("(%d, %d, %d)",
//                                    nmsBlockPos.getX(),
//                                    nmsBlockPos.getY(),
//                                    nmsBlockPos.getZ());
//
//
//                            FakeBlock fakeBlock = visibilityManager.getBlockAtPosForPlayer(player, loc);
//                            if (fakeBlock != null) {
//                                if (!visibilityManager.isBlockClickable(fakeBlock)) {
//                                    event.setCancelled(true);
//                                    plugin.getLogger().info("Cancelled -> [Proto][CLICK] (fake): " + strPos);
//                                    return;
//                                }
//                            }
//
//                            FakeSkull fakeSkull = visibilityManager.getSkullAtPosForPlayer(player, loc);
//                            if (fakeSkull != null) {
//                                if (!visibilityManager.isSkullClickable(fakeSkull)) {
//                                    event.setCancelled(true);
//                                    plugin.getLogger().info("Cancelled -> [Proto][CLICK] (fake): " + strPos);
//                                    return;
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                    }
//                }
//            }
//        });



        // Listen for block dig packets (both start and stop digging)
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
                    Player player = event.getPlayer();
                    PacketContainer packet = event.getPacket();

                    BlockPosition blockPos = packet.getBlockPositionModifier().read(0);
                    Location location = new Location(player.getWorld(),
                            blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    Material matType = location.getBlock().getType();

                    String strPos = String.format("(%d, %d, %d)",
                                location.getBlockX(),
                                location.getBlockY(),
                                location.getBlockZ());

                    EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().read(0);
                    if (digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
                        if (visibilityManager.isFakeSkullForPlayer(player, location)) {
                            if (matType == Material.AIR) {
                                //event.setCancelled(true);

                                if (BlockFaker.debug)
                                    plugin.getLogger().info("Cancelled -> [Proto][DIG] (fake): " + strPos);

                                visibilityManager.updateFakeOnEvent(player, location, 2);
                                // TODO: This kinda works. >= 2 ticks
                            }
                        }
                    }
                }
            }
        });

        // Listen for output block updates
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.BLOCK_CHANGE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
                    Player player = event.getPlayer();
                    PacketContainer packet = event.getPacket();
                    BlockPosition blockPos = packet.getBlockPositionModifier().read(0);

                    Location location = new Location(player.getWorld(),
                            blockPos.getX(), blockPos.getY(), blockPos.getZ());

                    String strPos = String.format("(%d, %d, %d)",
                            location.getBlockX(),
                            location.getBlockY(),
                            location.getBlockZ());

                    // TODO: Check if this block update has to be block for all players within the area
                    // This is not working as show/hide requires block update, need a lookup for that

                    FakeBlock fakeBlock = visibilityManager.getBlockAtPosForPlayer(player, location);
                    if (fakeBlock != null) {
                        if (!visibilityManager.shouldBlockUpdate(fakeBlock)) {
                            event.setCancelled(true);

                            if (BlockFaker.debug)
                                plugin.getLogger().info("Cancelled -> [Proto][BU-B] (fake): " + strPos);

                            return;
                        }
                    }

                    FakeSkull fakeSkull = visibilityManager.getSkullAtPosForPlayer(player, location);
                    if (fakeSkull != null) {
                        if (!visibilityManager.shouldSkullUpdate(fakeSkull)) {
                            event.setCancelled(true);

                            if (BlockFaker.debug)
                                plugin.getLogger().info("Cancelled -> [Proto][BU-S] (fake): " + strPos);
                        }
                    }
                }
            }
        });
    }
}