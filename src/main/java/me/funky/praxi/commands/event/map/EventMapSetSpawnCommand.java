package me.funky.praxi.commands.event.map;

import me.funky.praxi.event.game.map.EventGameMap;
import me.funky.praxi.event.game.map.impl.SpreadEventGameMap;
import me.funky.praxi.event.game.map.impl.TeamEventGameMap;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = {"event map set spawn", "event map add spawn"}, permission = "practice.admin.event")
public class EventMapSetSpawnCommand {

    public void execute(Player player, @CPL("map") EventGameMap map, @CPL("field") String field) {
        if (map == null) {
            player.sendMessage(CC.RED + "An event map with that name does not exist.");
        } else {
            switch (field.toLowerCase()) {
                case "spectator": {
                    map.setSpectatorPoint(player.getLocation());

                    player.sendMessage(CC.GREEN + "You successfully updated " +
                            map.getMapName() + "'s " + field + " location.");
                }
                break;
                case "a": {
                    if (!(map instanceof TeamEventGameMap)) {
                        player.sendMessage(CC.RED + "That type of map only has spread locations!");
                        player.sendMessage(CC.RED + "To add a location to the spread list, use " +
                                "/event map set <map> spread.");
                        break;
                    }

                    TeamEventGameMap teamMap = (TeamEventGameMap) map;
                    teamMap.setSpawnPointA(player.getLocation());

                    player.sendMessage(CC.GREEN + "You successfully updated " +
                            map.getMapName() + "'s " + field + " location.");
                }
                break;
                case "b": {
                    if (!(map instanceof TeamEventGameMap)) {
                        player.sendMessage(CC.RED + "That type of map only has spread locations!");
                        player.sendMessage(CC.RED + "To add a location to the spread list, use " +
                                "/event map set <map> spread.");
                        break;
                    }

                    TeamEventGameMap teamMap = (TeamEventGameMap) map;
                    teamMap.setSpawnPointB(player.getLocation());

                    player.sendMessage(CC.GREEN + "You successfully updated " +
                            map.getMapName() + "'s " + field + " location.");
                }
                break;
                case "spread": {
                    if (!(map instanceof SpreadEventGameMap)) {
                        player.sendMessage(CC.RED + "That type of map does not have spread locations!");
                        player.sendMessage(CC.RED + "To set one of the locations, use " +
                                "/event map set <map> <a/b>.");
                        break;
                    }

                    SpreadEventGameMap spreadMap = (SpreadEventGameMap) map;
                    spreadMap.getSpawnLocations().add(player.getLocation());

                    player.sendMessage(CC.GREEN + "You successfully added a location to " +
                            map.getMapName() + "'s " + field + " list.");
                }
                break;
                default:
                    player.sendMessage(CC.RED + "A field by that name does not exist.");
                    player.sendMessage(CC.RED + "Fields: spectator, a, b");
                    return;
            }

            map.save();
        }
    }

}
