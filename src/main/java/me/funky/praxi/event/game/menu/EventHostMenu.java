package me.funky.praxi.event.game.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.event.Event;
import me.funky.praxi.event.game.EventGame;
import me.funky.praxi.event.game.map.EventGameMap;
import me.funky.praxi.event.game.map.vote.EventGameMapVoteData;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.TextSplitter;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHostMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("EVENTS.TITLE");
    }

    @Override
    public int getSize() {
        return Praxi.getInstance().getMenusConfig().getInteger("EVENTS.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Praxi.getInstance().getMenusConfig().getString("EVENTS.FILTER"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (Event event : Event.events) {
            buttons.put(13, new SelectEventButton(event));
        }
        return buttons;
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

    @AllArgsConstructor
    private class SelectEventButton extends Button {

        private Event event;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);

            for (String descriptionLine : TextSplitter.split(28, event.getDescription(), "&7", " ")) {
                lore.add(" " + descriptionLine);
            }

            lore.add("");

            if (event.canHost(player)) {
                lore.add(ChatColor.GREEN + "Click to host event!");
            } else {
                lore.add(ChatColor.RED + "You cannot host this event.");
                lore.add(ChatColor.RED + "Purchase a rank upgrade on our store.");
            }

            lore.add(CC.MENU_BAR);

            return new ItemBuilder(event.getIcon().clone())
                    .name(Praxi.getInstance().getMenusConfig().getString("EVENTS.EVENT-NAME").replace("<event>", event.getDisplayName()))
                    .lore(lore)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (event.canHost(player)) {

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
                    player.sendMessage(CC.RED + "Types: sumo");
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
                } catch (Exception ignored) {
                }

                player.chat("/host " + event.getDisplayName());
            } else {
                player.sendMessage(ChatColor.RED + "You cannot host that event.");
            }
        }
    }
}
