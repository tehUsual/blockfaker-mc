package org.smaskee.blockFaker.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import org.smaskee.blockFaker.commands.blocks.*;
import org.smaskee.blockFaker.commands.skulls.*;
import org.smaskee.blockFaker.commands.textures.*;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final JavaPlugin plugin;
    private final Map<String, CommandExecutor> commands;

    public CommandRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();

    }

    public void registerCommand(String commandName, CommandExecutor executor) {
        commands.put(commandName, executor);
        PluginCommand pluginCommand = plugin.getCommand(commandName);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(executor);
            if (executor instanceof TabCompleter) {
                pluginCommand.setTabCompleter((TabCompleter) executor);
            }
        }
    }

    public void registerAllCommands() {
        // Block commands
        registerCommand("createfakeblock", new CreateFakeBlockCommand(plugin));
        registerCommand("removefakeblock", new RemoveFakeBlockCommand(plugin));
        registerCommand("listfakeblocks", new ListFakeBlocksCommand(plugin));
        registerCommand("togglefakeblock", new ToggleFakeBlockCommand(plugin));

        registerCommand("showfakeblocks", new ShowFakeBlocksCommand(plugin));
        registerCommand("hidefakeblocks", new HideFakeBlocksCommand(plugin));
        registerCommand("hideallfakeblocks", new HideAllFakeBlocksCommand(plugin));

        // Texture commands
        registerCommand("createtexturefromskull", new CreateTextureFromSkullCommand(plugin));
        registerCommand("removetexture", new RemoveTextureCommand(plugin));
        registerCommand("listtextures", new ListTexturesCommand(plugin));

        // Skull commands
        registerCommand("createfakeskull", new CreateFakeSkullCommand(plugin));
        registerCommand("createfakeskullfromblock", new CreateFakeSkullFromBlockCommand(plugin));
        registerCommand("removefakeskull", new RemoveFakeSkullCommand(plugin));
        registerCommand("listfakeskulls", new ListFakeSkullsCommand(plugin));

        registerCommand("togglefakeskull", new ToggleFakeSkullCommand(plugin));

        registerCommand("showfakeskulls", new ShowFakeSkullsCommand(plugin));
        registerCommand("hidefakeskulls", new HideFakeSkullsCommand(plugin));
        registerCommand("hideallfakeskulls", new HideAllFakeSkullsCommand(plugin));

        // Admin commands
        registerCommand("blockfaker", new ReloadCommand(plugin));
    }
} 