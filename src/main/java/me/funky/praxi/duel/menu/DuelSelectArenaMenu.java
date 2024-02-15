package me.funky.praxi.duel.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.arena.ArenaType;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DuelSelectArenaMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("DUEL.ARENA-SELECTOR.TITLE");
    }

    @Override
    public int getSize() {
        return Praxi.getInstance().getMenusConfig().getInteger("DUEL.ARENA-SELECTOR.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Praxi.getInstance().getMenusConfig().getString("DUEL.FILTER"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        Map<Integer, Button> buttons = new HashMap<>();
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
                    .name(Praxi.getInstance().getMenusConfig().getString("DUEL.ARENA-SELECTOR.ARENA-NAME").replace("<arena>", arena.getName()))
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

            // Force close inventory
            player.closeInventory();
        }

    }
}
