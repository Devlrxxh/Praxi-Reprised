package me.funky.praxi.duel.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DuelSelectKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("DUEL.KIT-SELECTOR.TITLE");
    }

    @Override
    public int getSize() {
        return Praxi.getInstance().getMenusConfig().getInteger("DUEL.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Praxi.getInstance().getMenusConfig().getString("DUEL.FILTER"));
    }


    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int i = 10;

        for (Kit kit : Kit.getKits()) {
            if (kit.isEnabled()) {
                buttons.put(i++, new SelectKitButton(kit));
            }
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
    private static class SelectKitButton extends Button {

        private Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(kit.getDisplayIcon())
                    .name(Praxi.getInstance().getMenusConfig().getString("DUEL.KIT-SELECTOR.KIT-NAME").replace("<kit>", kit.getName()))
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getDuelProcedure() == null) {
                player.sendMessage(CC.RED + "Could not find duel procedure.");
                return;
            }

            // Update duel procedure
            profile.getDuelProcedure().setKit(kit);

            // Set closed by menu
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            // Force close inventory
            player.closeInventory();

            // Open arena selection menu
            new DuelSelectArenaMenu().openMenu(player);
        }
    }
}
