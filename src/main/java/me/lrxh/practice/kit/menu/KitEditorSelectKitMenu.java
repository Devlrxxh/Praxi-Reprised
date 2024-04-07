package me.lrxh.practice.kit.menu;

import lombok.AllArgsConstructor;
import me.lrxh.practice.Practice;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.filters.Filters;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class KitEditorSelectKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Practice.getInstance().getMenusConfig().getString("KIT-EDITOR.TITLE");
    }

    @Override
    public int getSize() {
        return Practice.getInstance().getMenusConfig().getInteger("KIT-EDITOR.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("KIT-EDITOR.FILTER"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        AtomicInteger i = new AtomicInteger(10);
        Kit.getKits().forEach(kit -> {
            if (kit.isEnabled() && kit.getKitLoadout().getContents() != null) {
                buttons.put(i.getAndIncrement(), new KitDisplayButton(kit));
            }
        });

        return buttons;
    }

    @AllArgsConstructor
    private static class KitDisplayButton extends Button {

        private Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>(Practice.getInstance().getMenusConfig().getStringList("KIT-EDITOR.LORE"));
            return new ItemBuilder(kit.getDisplayIcon())
                    .name(Practice.getInstance().getMenusConfig().getString("KIT-EDITOR.KIT-NAME").replace("<kit>", kit.getName()))
                    .lore(lore, player)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            player.closeInventory();

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getKitEditorData().setSelectedKit(kit);

            new KitManagementMenu(kit).openMenu(player);
        }

    }
}
