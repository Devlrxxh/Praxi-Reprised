package me.funky.praxi.commands.event.admin;

import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandMeta(label = { "event admin" }, permission = "praxi.admin.event")
public class EventAdminCommand {

	private final String[][] HELP = new String[][] {
			new String[]{ "/event forcestart", "Force start the active event" },
			new String[]{ "/event cancel", "Cancel the active event" },
			new String[]{ "/event clearcd", "Clear the event cooldown" },
			new String[]{ "/event set lobby <event>", "Set lobby location" },
			new String[]{ "/event add map <event> <map>", "Allow a map to be played" },
			new String[]{ "/event remove map <event> <map>", "Deny a map to be played" },
			new String[]{ "/event map create <name>", "Create a map" },
			new String[]{ "/event map delete <name>", "Delete a map" },
			new String[]{ "/event map set spawn <name>", "Set a spawn point" },
			new String[]{ "/event map status <map>", "Check the status of a map" }
	};

	public void execute(Player player) {
		player.sendMessage(CC.CHAT_BAR);
		player.sendMessage(CC.GOLD + "Event Admin");

		for (String[] command : HELP) {
			player.sendMessage(CC.BLUE + command[0] + CC.GRAY + " - " + CC.WHITE + command[1]);
		}

		player.sendMessage(CC.CHAT_BAR);
	}

}
