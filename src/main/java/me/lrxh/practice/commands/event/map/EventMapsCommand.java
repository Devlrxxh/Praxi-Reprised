package me.lrxh.practice.commands.event.map;

import me.lrxh.practice.event.game.map.EventGameMap;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.command.command.CommandMeta;
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
