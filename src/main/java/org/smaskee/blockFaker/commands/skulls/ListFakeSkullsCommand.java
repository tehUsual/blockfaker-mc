package org.smaskee.blockFaker.commands.skulls;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.smaskee.blockFaker.commands.BaseCommand;
import org.smaskee.blockFaker.commands.CommandUtils;
import org.smaskee.blockFaker.structs.FakeSkull;

import java.util.Map;

public class ListFakeSkullsCommand extends BaseCommand {
    public ListFakeSkullsCommand(JavaPlugin plugin) {
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
        Map<String, FakeSkull> skulls = dataManager.getAllSkulls();
        for (String name : skulls.keySet()) {
            if (count > 30) break;

            FakeSkull skull = skulls.get(name);
            sender.sendMessage("FakeSkull: §9" + CommandUtils.locToStr(skull.getLocation()) + "§r, §a" + name
                + "§r, Tex(§b" + skull.getTextureName() + "§r)");
            count++;
        }

        if (count < skulls.size())
            sender.sendMessage("FakeSkull: §a .." + count + "more fake skulls");
        return true;
    }
}