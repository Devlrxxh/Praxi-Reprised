package me.funky.praxi.kit.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitEditorSelectKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("KIT-EDITOR.TITLE");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        Kit.getKits().forEach(kit -> {
            if (kit.isEnabled()) {
                buttons.put(buttons.size(), new KitDisplayButton(kit));
            }
        });

        return buttons;
    }

    @AllArgsConstructor
    private class KitDisplayButton extends Button {

        private Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>(Praxi.getInstance().getMenusConfig().getStringList("KIT-EDITOR.LORE"));
            return new ItemBuilder(kit.getDisplayIcon())
                    .name(Praxi.getInstance().getMenusConfig().getString("KIT-EDITOR.KIT-NAME").replace("<kit>", kit.getName()))
                    .lore(lore)
                    .clearFlags()
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
