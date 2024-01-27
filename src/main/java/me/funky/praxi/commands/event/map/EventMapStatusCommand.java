package me.funky.praxi.commands.event.map;

import me.funky.praxi.event.game.map.EventGameMap;
import me.funky.praxi.event.game.map.impl.SpreadEventGameMap;
import me.funky.praxi.event.game.map.impl.TeamEventGameMap;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import me.funky.praxi.util.CC;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.entity.Player;

@CommandMeta(label = "event map status", permission = "praxi.admin.event")
public class EventMapStatusCommand {

	public void execute(Player player, EventGameMap gameMap) {
		if (gameMap == null) {
			player.sendMessage(CC.RED + "An event map with that name does not exist.");
		} else {
			player.sendMessage(CC.GOLD + CC.BOLD + "Event Map Status " + CC.GRAY + "(" +
			                   (gameMap.isSetup() ? CC.GREEN : CC.RED) + gameMap.getMapName() + CC.GRAY + ")");
			player.sendMessage(CC.GREEN + "Spectator Location: " + CC.YELLOW +
			                   (gameMap.getSpectatorPoint() == null ?
					                   StringEscapeUtils.unescapeJava("\u2717") :
					                   StringEscapeUtils.unescapeJava("\u2713")));

			if (gameMap instanceof TeamEventGameMap) {
				TeamEventGameMap teamGameMap = (TeamEventGameMap) gameMap;

				player.sendMessage(CC.GREEN + "Spawn A Location: " + CC.YELLOW +
				                   (teamGameMap.getSpawnPointA() == null ?
						                   StringEscapeUtils.unescapeJava("\u2717") :
						                   StringEscapeUtils.unescapeJava("\u2713")));

				player.sendMessage(CC.GREEN + "Spawn B Location: " + CC.YELLOW +
				                   (teamGameMap.getSpawnPointB() == null ?
						                   StringEscapeUtils.unescapeJava("\u2717") :
						                   StringEscapeUtils.unescapeJava("\u2713")));
			} else if (gameMap instanceof SpreadEventGameMap) {
				SpreadEventGameMap spreadGameMap = (SpreadEventGameMap) gameMap;

				player.sendMessage(CC.GREEN + "Spread Locations: " + CC.YELLOW +
				                   (spreadGameMap.getSpawnLocations().isEmpty() ?
						                   StringEscapeUtils.unescapeJava("\u2717") :
						                   StringEscapeUtils.unescapeJava("\u2713")));
			}
		}
	}

}
