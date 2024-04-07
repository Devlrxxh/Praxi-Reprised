package me.lrxh.practice.setting;

import lombok.AllArgsConstructor;
import me.lrxh.practice.Practice;
import me.lrxh.practice.menus.StatsMenu;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.filters.Filters;
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
        return Practice.getInstance().getMenusConfig().getString("PROFILE-SETTINGS.TITLE");
    }

    @Override
    public int getSize() {
        return Practice.getInstance().getMenusConfig().getInteger("PROFILE-SETTINGS.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("PROFILE-SETTINGS.FILTER"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(14, new StatsButton());
        buttons.put(12, new SettingButton());

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
                    .lore(lore, player)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            player.closeInventory();
            new SettingsMenu().openMenu(player);
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
                    .lore(lore, player)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            new StatsMenu(player.getName()).openMenu(player);
        }
    }
}
