package me.funky.praxi.commands.event.map;

import me.funky.praxi.event.game.map.EventGameMap;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = "event maps", permission = "practice.admin.event")
public class EventMapsCommand {

    public void execute(Player player) {
        player.sendMessage(CC.GOLD + CC.BOLD + "Event Maps");

        if (EventGameMap.getMaps().isEmpty()) {
            player.sendMessage(CC.GRAY + "There are no event maps.");
        } else {
            for (EventGameMap gameMap : EventGameMap.getMaps()) {
                player.sendMessage(" - " + (gameMap.isSetup() ? CC.GREEN : CC.RED) + gameMap.getMapName());
            }
        }
    }

}
