package me.funky.praxi.util.menu.button;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class BackButton extends Button {

    private Menu back;

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.REDSTONE)
                .name(Praxi.getInstance().getMenusConfig().getString("KIT-EDITOR.MANAGEMENT.BUTTONS.BACK-BUTTON"))
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        Button.playNeutral(player);
        back.openMenu(player);
    }

}
