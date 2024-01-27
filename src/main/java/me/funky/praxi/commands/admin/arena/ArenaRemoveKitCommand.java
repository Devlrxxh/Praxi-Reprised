package me.funky.praxi.commands.admin.arena;

import me.funky.praxi.arena.Arena;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandMeta(label = "arena remove kit", permission = "praxi.admin.arena")
public class ArenaRemoveKitCommand {

	public void execute(CommandSender sender, @CPL("arena") Arena arena, @CPL("kit") Kit kit) {
		if (arena == null) {
			sender.sendMessage(ChatColor.RED + "An arena with that name does not exist.");
			return;
		}

		if (kit == null) {
			sender.sendMessage(ChatColor.RED + "A kit with that name does not exist.");
			return;
		}

		arena.getKits().remove(kit.getName());
		arena.save();

		sender.sendMessage(ChatColor.GOLD + "Removed kit \"" + kit.getName() +
		                   "\" from arena \"" + arena.getName() + "\"");
	}

}
