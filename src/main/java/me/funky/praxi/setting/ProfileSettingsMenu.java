package me.funky.praxi.setting;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.event.game.menu.EventHostMenu;
import me.funky.praxi.menus.StatsMenu;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSettingsMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("PROFILE-SETTINGS.TITLE");
    }

    @Override
    public int getSize() {
        return Praxi.getInstance().getMenusConfig().getInteger("PROFILE-SETTINGS.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Praxi.getInstance().getMenusConfig().getString("PROFILE-SETTINGS.FILTER"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(15, new StatsButton());
        buttons.put(13, new SettingButton());
        buttons.put(11, new EventHostButton());

        return buttons;
    }

    @AllArgsConstructor
    private class SettingButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.translate("&7View or change your"));
            lore.add(CC.translate("&7personal profile settings."));
            lore.add(CC.translate(""));
            lore.add(CC.translate("&aClick to open"));

            return new ItemBuilder(Material.REDSTONE_COMPARATOR)
                    .name("&bSettings")
                    .lore(lore)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            new SettingsMenu().openMenu(player);
        }
    }

    @AllArgsConstructor
    private class EventHostButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.translate("&7Host Events."));
            lore.add(CC.translate(""));
            lore.add(CC.translate("&aClick to open"));

            return new ItemBuilder(Material.PAPER)
                    .name("&bEvents")
                    .lore(lore)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            new EventHostMenu().openMenu(player);
        }
    }

    @AllArgsConstructor
    private class StatsButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.translate("&7View your personal"));
            lore.add(CC.translate("&7stats."));
            lore.add(CC.translate(""));
            lore.add(CC.translate("&aClick to open"));

            return new ItemBuilder(Material.EMERALD)
                    .name("&bStatistics")
                    .lore(lore)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            new StatsMenu().openMenu(player);
        }
    }
}
