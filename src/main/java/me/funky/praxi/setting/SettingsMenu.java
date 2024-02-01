package me.funky.praxi.setting;

import me.funky.praxi.Praxi;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingsMenu extends Menu {
    public SettingsMenu() {
        this.setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("SETTINGS.TITLE");
    }

    @Override
    public int getSize() {
        return Praxi.getInstance().getMenusConfig().getInteger("SETTINGS.SIZE");
    }

    @Override
    public boolean getFill() {
        return true;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        int i = 10;
        for (Settings settings : Settings.values()) {
            buttons.put(i++, new SettingsButton(settings));
        }
        return buttons;
    }

    private class SettingsButton extends Button {
        private final Settings settings;

        public SettingsButton(Settings settings) {
            this.settings = settings;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ArrayList<String> lore = new ArrayList<>();
            Profile profile = Profile.getByUuid(player.getUniqueId());
            lore.add("&7" + this.settings.getDescription());
            lore.add(" ");
            switch (this.settings) {
                case SHOW_SCOREBOARD: {
                    if (profile.getOptions().showScoreboard()) {
                        lore.add(" &7&l* &aYes");
                        lore.add(" &7&l* &7No");
                        lore.add(" ");
                        lore.add("&aClick to disable");
                        break;
                    }
                    lore.add(" &7&l* &7Yes");
                    lore.add(" &7&l* &cNo");
                    lore.add(" ");
                    lore.add("&aClick to enable");
                    break;
                }
                case ALLOW_DUELS: {
                    if (profile.getOptions().receiveDuelRequests()) {
                        lore.add(" &7&l* &aYes");
                        lore.add(" &7&l* &7No");
                        lore.add(" ");
                        lore.add("&aClick to disable");
                        break;
                    }
                    lore.add(" &7&l* &7Yes");
                    lore.add(" &7&l* &cNo");
                    lore.add(" ");
                    lore.add("&aClick to enable");
                    break;
                }
                case ALLOW_SPECTATORS: {
                    if (profile.getOptions().allowSpectators()) {
                        lore.add(" &7&l* &aYes");
                        lore.add(" &7&l* &7No");
                        lore.add(" ");
                        lore.add("&aClick to disable");
                        break;
                    }
                    lore.add(" &7&l* &7Yes");
                    lore.add(" &7&l* &cNo");
                    lore.add(" ");
                    lore.add("&aClick to enable");
                }
            }
            return new ItemBuilder(this.settings.getMaterial()).name("&c" + this.settings.getName()).lore(lore).clearEnchantments().clearFlags().clearFlags().build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            switch (this.settings) {
                case SHOW_SCOREBOARD: {
                    profile.getOptions().showScoreboard(!profile.getOptions().showScoreboard());
                    System.out.println(profile.getOptions().showScoreboard());
                    break;
                }
                case ALLOW_DUELS: {
                    profile.getOptions().receiveDuelRequests(!profile.getOptions().receiveDuelRequests());
                    break;
                }
                case ALLOW_SPECTATORS: {
                    profile.getOptions().allowSpectators(!profile.getOptions().allowSpectators());
                }
            }
            new SettingsMenu().openMenu(player);
            player.updateInventory();
        }
    }
}
 