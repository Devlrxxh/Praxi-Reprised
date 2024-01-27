package me.funky.praxi.commands.event.admin;

import me.funky.praxi.Praxi;
import me.funky.praxi.util.command.command.CommandMeta;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandMeta(label = { "event", "event help" })
public class EventHelpCommand {

	public void execute(Player player) {
		for (String line : Praxi.getInstance().getMainConfig().getStringList("EVENT.HELP")) {
			player.sendMessage(CC.translate(line));
		}
	}

}
