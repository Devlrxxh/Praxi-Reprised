package me.funky.praxi.profile.hotbar;

import lombok.Getter;
import me.funky.praxi.Praxi;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Hotbar {

    @Getter
    private static final Map<HotbarItem, ItemStack> items = new HashMap<>();

    public static void init() {
        FileConfiguration config = Praxi.getInstance().getMainConfig().getConfiguration();

        ConfigurationSection itemsSection = config.getConfigurationSection("HOTBAR_ITEMS");
        if (itemsSection == null) {
            return;
        }

        for (String section : itemsSection.getKeys(false)) {
            for (String itemName : config.getConfigurationSection("HOTBAR_ITEMS." + section).getKeys(false)) {
                String path = "HOTBAR_ITEMS." + section + "." + itemName + ".";
                HotbarItem hotbarItem = HotbarItem.valueOf(itemName);
                ItemBuilder builder = new ItemBuilder(Material.valueOf(config.getString(path + "MATERIAL")));
                builder.durability(config.getInt(path + "DURABILITY"));
                builder.name(config.getString(path + "NAME"));
                builder.lore(config.getStringList(path + "LORE"));
                builder.clearFlags();
                hotbarItem.setSlot(config.getInt(path + "SLOT"));
                if (section.equals("PARTY")) {
                    hotbarItem.setParty(true);
                } else {
                    hotbarItem.setState(ProfileState.valueOf(section));
                }
                items.put(hotbarItem, builder.build());
            }
        }


    }

    public static void giveHotbarItems(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerUtil.reset(player);

        switch (profile.getState()) {
            case LOBBY:
                if (profile.getParty() == null) {
                    for (HotbarItem item : items.keySet()) {
                        if (item.getState() != null && item.getState().equals(ProfileState.LOBBY) || item.equals(HotbarItem.KIT_EDITOR)) {
                            player.getInventory().setItem(item.getSlot(), items.get(item));
                        }
                    }
                } else {
                    for (HotbarItem item : items.keySet()) {
                        if (item.isParty()) {
                            player.getInventory().setItem(item.getSlot(), items.get(item));
                        }
                    }
                }
                break;
            case QUEUEING:
            case SPECTATING:
            case EVENT:
                for (HotbarItem item : items.keySet()) {
                    if (item.getState() != null && item.getState().equals(profile.getState())) {
                        player.getInventory().setItem(item.getSlot(), items.get(item));

                    }
                }
                break;
        }
        player.updateInventory();
    }

    public static HotbarItem fromItemStack(ItemStack itemStack) {
        for (Map.Entry<HotbarItem, ItemStack> entry : Hotbar.getItems().entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(itemStack)) {
                return entry.getKey();
            }
        }

        return null;
    }
}
