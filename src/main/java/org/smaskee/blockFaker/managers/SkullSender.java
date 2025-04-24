package org.smaskee.blockFaker.managers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.smaskee.blockFaker.BlockFaker;
import org.smaskee.blockFaker.structs.FakeSkull;
import org.smaskee.blockFaker.structs.SkullTexture;

import java.util.UUID;

public class SkullSender {
    private final Plugin plugin;
    private final DataManager dataManager;

    public SkullSender(BlockFaker plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    private static UUID intArrayToUUID(int[] array) {
        if (array.length != 4)
            return null;

        long most = ((long)array[0] << 32) | (array[1] & 0xFFFFFFFFL);
        long least = ((long)array[2] << 32) | (array[3] & 0xFFFFFFFFL);
        return new UUID(most, least);
    }

    public static int[] uuidToIntArray(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        return new int[] {
                (int)(most >>> 32),
                (int)most,
                (int)(least >>> 32),
                (int)least
        };
    }

    private CompoundTag createSkullNBT(FakeSkull fakeSkull) {
        SkullTexture skullTexture = dataManager.getTexture(fakeSkull.getTextureName());
        if (skullTexture == null)
            return new CompoundTag();

        String textureValue = skullTexture.getValue();
        if (textureValue == null)
            return new CompoundTag();

        CompoundTag nbt = new CompoundTag();
        CompoundTag skullOwner = new CompoundTag();

        // Create UUID array
        int[] uuidArray = uuidToIntArray(skullTexture.getUuid());
        skullOwner.putIntArray("Id", uuidArray);

        // Properties
        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag textureData = new CompoundTag();
        textureData.putString("Value", textureValue);

        // Assemble
        textures.add(textureData);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        nbt.put("SkullOwner", skullOwner);

        return nbt;
    }

    private static BlockPos createBlockPos(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static BlockState createBlockState(FakeSkull fakeSkull) {
        if (fakeSkull.isWallSkull()) {
            return Blocks.PLAYER_WALL_HEAD.defaultBlockState().setValue(
                    WallSkullBlock.FACING,
                    Direction.valueOf(fakeSkull.getRotation().name())
            );
        } else {
            return Blocks.PLAYER_HEAD.defaultBlockState().setValue(
                    SkullBlock.ROTATION, getSkullRotation(fakeSkull.getRotation()));
        }
    }

    private static int getSkullRotation(BlockFace face) {
        return switch (face) {
            case NORTH -> 0;
            case NORTH_NORTH_EAST -> 1;
            case NORTH_EAST -> 2;
            case EAST_NORTH_EAST -> 3;
            case EAST -> 4;
            case EAST_SOUTH_EAST -> 5;
            case SOUTH_EAST -> 6;
            case SOUTH_SOUTH_EAST -> 7;
            case SOUTH -> 8;
            case SOUTH_SOUTH_WEST -> 9;
            case SOUTH_WEST -> 10;
            case WEST_SOUTH_WEST -> 11;
            case WEST -> 12;
            case WEST_NORTH_WEST -> 13;
            case NORTH_WEST -> 14;
            case NORTH_NORTH_WEST -> 15;
            default -> 0; // Default to NORTH if invalid
        };
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

    private void sendSkullPacket(Player player, FakeSkull fakeSkull) {
        // Create block position and state
        BlockPos nmsBlockPos = createBlockPos(fakeSkull.getLocation());
        BlockState nmsBlockState = createBlockState(fakeSkull);

        // Create skull entity
        SkullBlockEntity nmsSkullEntity = new SkullBlockEntity(nmsBlockPos, nmsBlockState);
        CompoundTag nbtSkull = createSkullNBT(fakeSkull);
        nmsSkullEntity.load(nbtSkull);

        // Get connection
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        // Send block update packet
        nmsPlayer.connection.send(new ClientboundBlockUpdatePacket(nmsBlockPos, nmsBlockState));
        // Send tile entity data packet
        ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(
                nmsSkullEntity,
                BlockEntity::saveWithFullMetadata
        );
        nmsPlayer.connection.send(packet);
    }

    private void sendRealBlockAt(Player player, Location location) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel nmsLevel = nmsPlayer.getLevel();
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

    public void sendSkull(Player player, FakeSkull fakeSkull, boolean sendFake) {
        if (!player.isOnline())
            return;

        if (dataManager.getTexture(fakeSkull.getTextureName()) == null)
            return;

        if (!isPlayerNearby(player, fakeSkull.getLocation())) {
            if (BlockFaker.debug)
                plugin.getLogger().info("[SendSkull]: player not nearby " + fakeSkull.getName());
            return;
        }


        if (sendFake) {
            sendSkullPacket(player, fakeSkull);
        } else {
            sendRealBlockAt(player, fakeSkull.getLocation());
        }
    }

    public void sendMultipleSkulls(Player player, FakeSkull[] fakeSkulls, boolean sendFake) {
        for (FakeSkull fakeSkull : fakeSkulls) {
            sendSkull(player, fakeSkull, sendFake);
        }
    }
}













