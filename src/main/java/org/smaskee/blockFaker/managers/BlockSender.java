package org.smaskee.blockFaker.managers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.structs.FakeBlock;


public class BlockSender {
    private final Plugin plugin;
    private final DataManager dataManager;

    public BlockSender(BlockFaker plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    private static BlockPos createBlockPos(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static BlockState createBlockState(String materialName) {
        ResourceLocation id = ResourceLocation.tryParse("minecraft:" + materialName.toLowerCase());
        if (id == null) return null;

        return BuiltInRegistries.BLOCK.get(id)
                .map(Holder.Reference::value)
                .map(Block::defaultBlockState)
                .orElse(null);
    }


    private static boolean isPlayerNearby(Player player, Location location) {
        int playerChunkX = player.getLocation().getBlockX() >> 4;
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;
        int viewDistance = Bukkit.getViewDistance();

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        if (Math.abs(chunkX - playerChunkX) > viewDistance || Math.abs(chunkZ - playerChunkZ) > viewDistance)
            return false;
        return true;
    }

    private void sendBlockPacket(Player player, FakeBlock fakeBlock) {
        BlockPos nmsBlockPos = createBlockPos(fakeBlock.getLocation());
        BlockState nmsBlockState = createBlockState(fakeBlock.getMaterial().name());
        if (nmsBlockState == null)
            return;

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.connection.send(new ClientboundBlockUpdatePacket(nmsBlockPos, nmsBlockState));
    }

    private void sendRealBlockAt(Player player, Location location) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        Level nmsLevel = nmsPlayer.level();
        BlockPos nmsBlockPos = createBlockPos(location);

        // Send block state
        nmsPlayer.connection.send(new ClientboundBlockUpdatePacket(nmsBlockPos, nmsLevel.getBlockState(nmsBlockPos)));

        BlockEntity blockEntity = nmsLevel.getBlockEntity(nmsBlockPos);
        if (blockEntity != null) {
            ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(
                    blockEntity,
                    BlockEntity::saveWithFullMetadata
            );
            nmsPlayer.connection.send(packet);
        }
    }

    public void sendBlock(Player player, FakeBlock fakeBlock, boolean sendFake) {
        if (!player.isOnline())
            return;

        if (!isPlayerNearby(player, fakeBlock.getLocation())) {
            if (BlockFaker.debug)
                plugin.getLogger().info("[SendBlock]: player not nearby " + fakeBlock.getName());
            return;
        }


        if (sendFake) {
            sendBlockPacket(player, fakeBlock);
        } else {
            sendRealBlockAt(player, fakeBlock.getLocation());
        }
    }


    public void sendMultipleBlocks(Player player, FakeBlock[] fakeBlocks, boolean sendFake) {
        for (FakeBlock fakeBlock : fakeBlocks) {
            sendBlock(player, fakeBlock, sendFake);
        }
    }
}
