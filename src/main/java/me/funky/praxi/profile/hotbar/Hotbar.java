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
                hotbarItem.setSlot(config.getInt(path + "SLOT"));
                if (section.equals("PARTY")) {
                    hotbarItem.setParty(true);
                } else {
                    hotbarItem.setState(ProfileState.valueOf(section));
                }
                System.out.println(path);
                items.put(hotbarItem, builder.build());
            }
        }


        //TODO: FIX THIS

        //Map<HotbarItem, String> dynamicContent = new HashMap<>();
        //dynamicContent.put(HotbarItem.MAP_SELECTION, "%MAP%");
        //dynamicContent.put(HotbarItem.KIT_SELECTION, "%KIT%");

        //for (Map.Entry<HotbarItem, String> entry : dynamicContent.entrySet()) {
        //    String voteName = Hotbar.getItems().get(entry.getKey()).getItemMeta().getDisplayName();
        //    String[] nameSplit = voteName.split(entry.getValue());

        //    entry.getKey().setPattern(
        //            Pattern.compile("(" + nameSplit[0] + ")(.*)(" + (nameSplit.length > 1 ? nameSplit[1] : "") + ")"));
        //}
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
                for (HotbarItem item : items.keySet()) {
                    if (item.getState() != null && item.getState().equals(ProfileState.QUEUEING)) {
                        player.getInventory().setItem(item.getSlot(), items.get(item));

                    }
                }
                break;
            case SPECTATING:
                for (HotbarItem item : items.keySet()) {
                    if (item.getState() != null && item.getState().equals(ProfileState.SPECTATING)) {
                        player.getInventory().setItem(item.getSlot(), items.get(item));


                    }
                }
                break;
            case EVENT:
                for (HotbarItem item : items.keySet()) {
                    if (item.getState() != null && item.getState().equals(ProfileState.EVENT)) {
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
