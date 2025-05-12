package org.smaskee.blockFaker.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.smaskee.blockFaker.helpers.WorldResolver;
import org.smaskee.blockFaker.managers.DataManager;
import org.smaskee.blockFaker.structs.FakeBlock;
import org.smaskee.blockFaker.structs.FakeSkull;

public class CommandUtils {
    public static boolean validateArgsLength(CommandSender sender, Command command, String[] args, int expectedLength) {
        if (args.length != expectedLength) {
            sender.sendMessage("§c" + command.getUsage());
            return false;
        }
        return true;
    }

    public static boolean validateArgsMinLength(CommandSender sender, Command command, String[] args, int minLength) {
        if (args.length < minLength) {
            sender.sendMessage("§c" + command.getUsage());
            return false;
        }
        return true;
    }

    public static boolean validateName(CommandSender sender, String name) {
        if (!name.matches("^[a-zA-Z0-9_]{1,16}$")) {
            sender.sendMessage("§cInvalid name! Use 1-16 letters, numbers, or underscores.");
            return false;
        }
        return true;
    }

    public static boolean validateMaterial(CommandSender sender, String input) {
        Material material = Material.matchMaterial(input);
        if (material == null) {
            sender.sendMessage("§cUnknown material: '" + input + "'");
            return false;
        }
        return true;
    }

    public static boolean isTextureCreated(CommandSender sender, String name, DataManager dataManager) {
        if (dataManager.getTexture(name) == null) {
            sender.sendMessage("§cCould not find texture '" + name + "'.");
            return false;
        }
        return true;
    }

    public static boolean validateLocation(CommandSender sender, Command command,
                                           String sx, String sy, String sz) {
        String worldName = WorldResolver.getOverworld().getName();
        return validateLocation(sender, command, sx, sy, sz, worldName);
    }

    public static boolean validateLocation(CommandSender sender, Command command,
                                           String sx, String sy, String sz, String worldName) {
        try {
            double x = Double.parseDouble(sx);
            double y = Double.parseDouble(sy);
            double z = Double.parseDouble(sz);
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                sender.sendMessage("§cInvalid world name: '" + worldName + "'");
                return false;
            }

            if (x < -30000000 || x > 30000000 || z < -30000000 || z > 30000000) {
                sender.sendMessage("§cCoordinates are outside the valid range!");
                return false;
            }

            if (y < world.getMinHeight() || y > world.getMaxHeight()) {
                sender.sendMessage("§cY coordinate is outside the valid range!");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid coordinates. " + command.getUsage());
            return false;
        }
        return true;
    }

    public static Location argToLocation(String posX, String posY, String posZ, String world) {
        return new Location(
                Bukkit.getWorld(world),
                Double.parseDouble(posX),
                Double.parseDouble(posY),
                Double.parseDouble(posZ)
        );
    }

    public static String locToStr(Location loc) {
        return String.format("[%s](%d, %d, %d)",
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ());
    }

    public static boolean isFakeBlockNameAvailable(CommandSender sender, String name, DataManager dataManager) {
        if (dataManager.getBlock(name) != null) {
            sender.sendMessage("§cA fake block with that name already exists!");
            return false;
        }
        return true;
    }

    public static boolean isTextureNameAvailable(CommandSender sender, String name, DataManager dataManager) {
        if (dataManager.getTexture(name) != null) {
            sender.sendMessage("§cA texture with the name '" + name + "' already exists!");
            return false;
        }
        return true;
    }

    public static boolean isFakeSkullNameAvailable(CommandSender sender, String name, DataManager dataManager) {
        if (dataManager.getSkull(name) != null) {
            sender.sendMessage("§cA fake skull with the name '" + name + "' already exists!");
            return false;
        }
        return true;
    }

    public static boolean isFakePositionAvailable(CommandSender sender, Location location, DataManager dataManager) {
        if (dataManager.getBlockAtPosition(location) != null) {
            sender.sendMessage("§cThere is already a fake block at that location!");
            return false;
        }
        return true;
    }
} 