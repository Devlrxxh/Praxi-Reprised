package me.funky.praxi.commands.event.user;

import me.funky.praxi.Praxi;
import me.funky.praxi.event.Event;
import me.funky.praxi.event.game.EventGame;
import me.funky.praxi.event.game.map.EventGameMap;
import me.funky.praxi.event.game.map.vote.EventGameMapVoteData;
import me.funky.praxi.event.game.menu.EventHostMenu;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandMeta(label = {"host", "event host"}, permission = "praxi.event.host")
public class EventHostCommand {

    public void execute(Player player) {
        if (player.hasMetadata("frozen")) {
            player.sendMessage(ChatColor.RED + "You cannot host an event while frozen.");
            return;
        }

        new EventHostMenu().openMenu(player);
    }

    public void execute(Player player, @CPL("event") Event event) {
        if (player.hasMetadata("frozen")) {
            player.sendMessage(ChatColor.RED + "You cannot host an event while frozen.");
            return;
        }

        if (EventGame.getActiveGame() != null) {
            player.sendMessage(CC.RED + "There is already an active event.");
            return;
        }

        if (!EventGame.getCooldown().hasExpired()) {
            player.sendMessage(CC.RED + "The event cooldown is active.");
            return;
        }

        if (event == null) {
            player.sendMessage(CC.RED + "That type of event does not exist.");
            player.sendMessage(CC.RED + "Types: sumo, corners");
            return;
        }

        if (EventGameMap.getMaps().isEmpty()) {
            player.sendMessage(CC.RED + "There are no available event maps.");
            return;
        }

        List<EventGameMap> validMaps = new ArrayList<>();

        for (EventGameMap gameMap : EventGameMap.getMaps()) {
            if (event.getAllowedMaps().contains(gameMap.getMapName())) {
                validMaps.add(gameMap);
            }
        }

        if (validMaps.isEmpty()) {
            player.sendMessage(CC.RED + "There are no available event maps.");
            return;
        }

        try {
            EventGame game = new EventGame(event, player, getHostSlots(player));

            for (EventGameMap gameMap : validMaps) {
                game.getVotesData().put(gameMap, new EventGameMapVoteData());
            }

            game.broadcastJoinMessage();
            game.start();
            game.getGameLogic().onJoin(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(Player player, @CPL("event") Event event, Integer slots) {
        if (!player.hasPermission("praxi.event.host.slots")) {
            if (slots < 4 || slots > 200) {
                player.sendMessage(CC.RED + "Events can only hold 4-200 players.");
                return;
            }
        }

        if (EventGame.getActiveGame() != null) {
            player.sendMessage(CC.RED + "There is already an active event.");
            return;
        }

        if (!EventGame.getCooldown().hasExpired()) {
            player.sendMessage(CC.RED + "The event cooldown is active.");
            return;
        }

        if (event == null) {
            player.sendMessage(CC.RED + "That type of event does not exist.");
            player.sendMessage(CC.RED + "Types: sumo, corners");
            return;
        }

        if (EventGameMap.getMaps().isEmpty()) {
            player.sendMessage(CC.RED + "There are no available event maps.");
            return;
        }

        List<EventGameMap> validMaps = new ArrayList<>();

        for (EventGameMap gameMap : EventGameMap.getMaps()) {
            if (event.getAllowedMaps().contains(gameMap.getMapName())) {
                validMaps.add(gameMap);
            }
        }

        if (validMaps.isEmpty()) {
            player.sendMessage(CC.RED + "There are no available event maps.");
            return;
        }

        try {
            EventGame game = new EventGame(event, player, slots);

            for (EventGameMap gameMap : validMaps) {
                game.getVotesData().put(gameMap, new EventGameMapVoteData());
            }

            game.broadcastJoinMessage();
            game.start();
            game.getGameLogic().onJoin(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getHostSlots(Player host) {
        int slots = 32;
        FileConfiguration config = Praxi.getInstance().getEventsConfig().getConfiguration();

        for (String key : config.getConfigurationSection("HOST_SLOTS").getKeys(false)) {
            if (host.hasPermission(config.getString("HOST_SLOTS." + key + ".PERMISSION"))) {
                if (config.getInt("HOST_SLOTS." + key + ".SLOTS") > slots) {
                    slots = config.getInt("HOST_SLOTS." + key + ".SLOTS");
                }
            }
        }

        return slots;
    }

}
