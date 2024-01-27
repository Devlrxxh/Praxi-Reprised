package me.funky.praxi.commands.event.admin;

import me.funky.praxi.event.Event;
import me.funky.praxi.event.game.map.EventGameMap;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandMeta(label = "event remove map", permission = "praxi.admin.event")
public class EventRemoveMapCommand {

	public void execute(Player player, @CPL("event") Event event, @CPL("map") EventGameMap gameMap) {
		if (event == null) {
			player.sendMessage(CC.RED + "An event type by that name does not exist.");
			player.sendMessage(CC.RED + "Types: sumo, corners");
			return;
		}

		if (gameMap == null) {
			player.sendMessage(CC.RED + "A map with that name does not exist.");
			return;
		}

		if (event.getAllowedMaps().remove(gameMap.getMapName())) {
			event.save();

			player.sendMessage(CC.GREEN + "You successfully removed the \"" + gameMap.getMapName() +
			                   "\" map from the \"" + event.getDisplayName() + "\" event.");
		}
	}

}
