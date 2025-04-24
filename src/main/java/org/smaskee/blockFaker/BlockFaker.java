package org.smaskee.blockFaker;

import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.CommandRegistry;
import org.smaskee.blockFaker.listeners.BlockInteractionPacketListener;
import org.smaskee.blockFaker.managers.BlockSender;
import org.smaskee.blockFaker.managers.DataManager;
import org.smaskee.blockFaker.managers.SkullSender;
import org.smaskee.blockFaker.managers.VisibilityManager;

import java.util.logging.Level;

public final class BlockFaker extends JavaPlugin {
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
        instance = this;
        dataManager = new DataManager(this, getDataFolder());
        blockSender = new BlockSender(this);
        skullSender = new SkullSender(this);
        visibilityManager = new VisibilityManager(this);
        commandRegistry = new CommandRegistry(this);
        packetListener = new BlockInteractionPacketListener(this);

        // Register all commands
        commandRegistry.registerAllCommands();

        getLogger().info("BlockFaker has been \u001B[32menabled!");
        if (debug)
            getLogger().info("Debug mode \u001B[33menabled!");
    }

    @Override
    public void onDisable() {
        dataManager.saveData();
        instance = null;

        if (debug)
            getLogger().log(Level.INFO, "\u001B[35m[Saved] onDisable()");

        getLogger().info("BlockFaker has been \u001B[32mdisabled!");
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
}