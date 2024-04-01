package me.lrxh.practice.duel.menu;

import lombok.AllArgsConstructor;
import me.lrxh.practice.Practice;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.arena.ArenaType;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.InventoryUtil;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.filters.Filters;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DuelSelectArenaMenu extends Menu {
    Map<Integer, Button> buttons = new HashMap<>();

    @Override
    public String getTitle(Player player) {
        return Practice.getInstance().getMenusConfig().getString("DUEL.ARENA-SELECTOR.TITLE");
    }

    @Override
    public int getSize() {
        return InventoryUtil.getMenuSize(buttons.size(), true);
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("DUEL.FILTER"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        int i = 10;

        for (Arena arena : Arena.getArenas()) {
            if (!arena.isSetup()) {
                continue;
            }

            if (!arena.getKits().contains(profile.getDuelProcedure().getKit().getName())) {
                continue;
            }

            if (profile.getDuelProcedure().getKit().getGameRules().isBuild()) {
                if (arena.getType() == ArenaType.SHARED) {
                    continue;
                }

                if (arena.getType() != ArenaType.STANDALONE) {
                    continue;
                }

                if (arena.isActive()) {
                    continue;
                }
            }

            buttons.put(i++, new SelectArenaButton(arena));
        }

        return buttons;
    }

    @Override
    public void onClose(Player player) {
        if (!isClosedByMenu()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setDuelProcedure(null);
        }
    }

    @AllArgsConstructor
    private static class SelectArenaButton extends Button {

        private Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .name(Practice.getInstance().getMenusConfig().getString("DUEL.ARENA-SELECTOR.ARENA-NAME").replace("<arena>", arena.getDisplayName()))
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            // Update and request the procedure
            profile.getDuelProcedure().setArena(this.arena);
            profile.getDuelProcedure().send();

            // Set closed by menu
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            // Play Sound

            Button.playSuccess(player);

            // Force close inventory
            player.closeInventory();
        }

    }
}
