package org.smaskee.blockFaker.commands.textures;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.structs.SkullTexture;

import java.util.Map;

public class ListTexturesCommand extends BaseCommand {
    public ListTexturesCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("blockfaker.read");
    }

    @Override
    protected boolean validateCommand(CommandSender sender, Command command, String[] args) {
        return true; // No arguments needed for this command
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String[] args) {
        int count = 0;
        Map<String, SkullTexture> textures = dataManager.getAllTextures();
        for (String name : textures.keySet()) {
            if (count > 30) break;

            sender.sendMessage("Texture: §a" + name);
            count++;
        }

        if (count < textures.size())
            sender.sendMessage("Texture: §a .." + count + "more textures");
        return true;
    }
}