package org.smaskee.blockFaker;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.CommandRegistry;
import org.smaskee.blockFaker.helpers.ANSI;
import org.smaskee.blockFaker.listeners.BlockInteractionPacketListener;
import org.smaskee.blockFaker.managers.BlockSender;
import org.smaskee.blockFaker.managers.DataManager;
import org.smaskee.blockFaker.managers.SkullSender;
import org.smaskee.blockFaker.managers.VisibilityManager;

import java.util.logging.Level;

public final class BlockFaker extends JavaPlugin {
    private HolderLookup.Provider registries;
    private DataManager dataManager;
    private BlockSender blockSender;
    private SkullSender skullSender;
    private VisibilityManager visibilityManager;
    private CommandRegistry commandRegistry;
    private BlockInteractionPacketListener packetListener;

    public static final boolean debug = false;

    private static BlockFaker instance;

    @Override
    public void onEnable() {
        // Init nms registries
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        if (server == null) {
            getServer().getPluginManager().disablePlugin(this);
            registries = null;
        } else {
            registries = server.registryAccess();
        }


        // Load plugin
        instance = this;
        dataManager = new DataManager(this, getDataFolder());
        blockSender = new BlockSender(this);
        skullSender = new SkullSender(this);
        visibilityManager = new VisibilityManager(this);
        commandRegistry = new CommandRegistry(this);
        packetListener = new BlockInteractionPacketListener(this);

        // Register all commands
        commandRegistry.registerAllCommands();

        getLogger().info("BlockFaker has been \u001B[32menabled!\u001B[0m");
        if (debug)
            getLogger().info("Debug mode \u001B[33menabled!\u001B[0m");
    }

    @Override
    public void onDisable() {
        dataManager.saveData();
        instance = null;

        if (debug)
            getLogger().log(Level.INFO, "\u001B[35m[Saved] onDisable()\u001B[0m");

        getLogger().info("BlockFaker has been \u001B[32mdisabled!\u001B[0m");
    }

    public HolderLookup.Provider getRegistries() {
        return registries;
    }

    public static BlockFaker getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public BlockSender getBlockSender() {
        return blockSender;
    }

    public SkullSender getSkullSender() {
        return skullSender;
    }

    public VisibilityManager getVisibilityManager() {
        return visibilityManager;
    }

    public void logDebug(String msg, String ansi) {
        if (debug) {
            getLogger().info(String.format("%s%s%s", ansi, msg, ANSI.RESET));
        }
    }

    public void logDebug(String label, String msg, String ansi) {
        logDebug(String.format("[%s]: %s", label, msg), ansi);
    }
}