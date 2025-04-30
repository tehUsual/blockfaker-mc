package org.smaskee.blockFaker.structs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rotatable;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.BlockFaker;

import java.util.UUID;

public class SkullBlock {
    private final Material type;
    private final String ownerName;
    private final String textureValue;
    private final UUID skullId;
    private final BlockFace rotation;
    private final boolean isWallSkull;

    public static final String[] VALID_FACES = {
            "NORTH", "NORTH_NORTH_EAST", "NORTH_EAST", "EAST_NORTH_EAST",
            "EAST", "EAST_SOUTH_EAST", "SOUTH_EAST", "SOUTH_SOUTH_EAST",
            "SOUTH", "SOUTH_SOUTH_WEST", "SOUTH_WEST", "WEST_SOUTH_WEST",
            "WEST", "WEST_NORTH_WEST", "NORTH_WEST", "NORTH_NORTH_WEST"};

    private SkullBlock(Material type, String ownerName, String textureValue, UUID skullId, BlockFace rotation, boolean isWallSkull) {
        this.type = type;
        this.ownerName = ownerName;
        this.textureValue = textureValue;
        this.skullId = skullId;
        this.rotation = rotation;
        this.isWallSkull = isWallSkull;
    }


    public static SkullBlock loadSkullFromBlock(Location location, JavaPlugin plugin) {
        // Verify skull
        Block block = location.getBlock();
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) {
            return null;
        }

        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);

        // Not a skull
        if (!(blockEntity instanceof SkullBlockEntity skull)) {
            return null;
        }

        CompoundTag nbt = skull.saveWithFullMetadata();
        String ownerName = null;
        String textureValue = null;
        UUID skullId = null;
        BlockFace rotation = BlockFace.SOUTH;
        boolean isWallSkull = block.getType() == Material.PLAYER_WALL_HEAD;

        if (BlockFaker.debug)
            plugin.getLogger().info("[SkullOrg]: NBT: " + nbt.toString());

        // Get skull owner data
        if (nbt.contains("SkullOwner")) {
            CompoundTag skullOwner = nbt.getCompound("SkullOwner");

            // Get skull ID
            if (skullOwner.contains("Id")) {
                int[] intArray = skullOwner.getIntArray("Id");
                if (intArray.length == 4) {
                    long mostSigBits = (((long) intArray[0]) << 32) | (intArray[1] & 0xFFFFFFFFL);
                    long leastSigBits = (((long) intArray[2]) << 32) | (intArray[3] & 0xFFFFFFFFL);
                    skullId = new UUID(mostSigBits, leastSigBits);
                }
            }

            // Get skull name
            if (skullOwner.contains("Name")) {
                ownerName = skullOwner.getString("Name");
            }

            // Get skull texture
            if (skullOwner.contains("Properties")) {
                CompoundTag prop = skullOwner.getCompound("Properties");
                if (prop.contains("textures")) {
                    ListTag textures = prop.getList("textures", Tag.TAG_COMPOUND);
                    if (!textures.isEmpty()) {
                        CompoundTag texture = textures.getCompound(0);
                        if (texture.contains("Value")) {
                            textureValue = texture.getString("Value");
                        }
                    }
                }
            }
        }

        // Get rotation
        if (block.getBlockData() instanceof Rotatable rotatable) {
            rotation = rotatable.getRotation().getOppositeFace();
        }

        return new SkullBlock(block.getType(), ownerName, textureValue, skullId, rotation, isWallSkull);
    }

    // Getters
    public Material getType() {
        return type;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getTextureValue() {
        return textureValue;
    }

    public UUID getSkullId() {
        return skullId;
    }

    public BlockFace getRotation() {
        return rotation;
    }

    public boolean isWallSkull() {
        return isWallSkull;
    }
}