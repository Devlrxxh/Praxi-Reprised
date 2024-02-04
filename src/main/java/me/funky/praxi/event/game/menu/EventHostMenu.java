package me.funky.praxi.event.game.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.event.Event;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.TextSplitter;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.button.DisplayButton;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

public class EventHostMenu extends Menu {

    {
        setPlaceholder(true);
    }

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
                lore.add(ChatColor.GREEN + "You can host this event.");
                lore.add(ChatColor.GREEN + "Maximum Slots: " + ChatColor.YELLOW + 50);
            } else {
                lore.add(ChatColor.RED + "You cannot host this event.");
                lore.add(ChatColor.RED + "Purchase a rank upgrade on our store.");
            }

            lore.add(CC.MENU_BAR);

            return new ItemBuilder(event.getIcon().clone())
                    .name("&6" + event.getDisplayName())
                    .lore(lore)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (event.canHost(player)) {
                player.chat("/host " + event.getDisplayName());
            } else {
                player.sendMessage(ChatColor.RED + "You cannot host that event.");
            }
        }

    }

}
