package me.funky.praxi.commands.admin.kits;

import me.funky.praxi.kit.Kit;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandMeta(label = "kits", permission = "praxi.admin.kit")
public class KitsCommand {

    public void execute(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Kits");

        for (Kit kit : Kit.getKits()) {
            sender.sendMessage(kit.getName());
        }
    }

}
