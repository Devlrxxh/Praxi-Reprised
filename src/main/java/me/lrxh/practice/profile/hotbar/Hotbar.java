package me.lrxh.practice.profile.hotbar;

import lombok.Getter;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
public class Hotbar {

    private final Map<HotbarItem, ItemStack> items = new HashMap<>();

    public void init() {
        FileConfiguration config = Practice.getInstance().getMainConfig().getConfiguration();

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
                    hotbarItem.setStaffmode(false);
                } else {
                    hotbarItem.setState(ProfileState.valueOf(section));
                }
                items.put(hotbarItem, builder.build());
            }
        }
        HotbarItem hotbarItem = HotbarItem.KIT_SELECTION;
        ItemBuilder builder = new ItemBuilder(Material.ENCHANTED_BOOK);
        builder.durability(0);
        builder.name("&e%KIT%");
        builder.lore("");
        builder.clearFlags();
        items.put(hotbarItem, builder.build());
        for (HotbarItem kitEditorItem : items.keySet()) {
            if (kitEditorItem.equals(HotbarItem.KIT_SELECTION)) {
                String voteName = getItems().get(HotbarItem.KIT_SELECTION).getItemMeta().getDisplayName();
                String[] nameSplit = voteName.split("%KIT%");
                kitEditorItem.setPattern(
                        Pattern.compile("(" + nameSplit[0] + ")(.*)(" + (nameSplit.length > 1 ? nameSplit[1] : "") + ")"));
            }
        }
    }

    public void giveHotbarItems(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerUtil.reset(player);

        switch (profile.getState()) {
            case LOBBY:

                for (HotbarItem item : items.keySet()) {
                    if (profile.getParty() == null) {
                        if (item.getState() != null && item.getState().equals(ProfileState.LOBBY) || item.equals(HotbarItem.KIT_EDITOR)) {
                            player.getInventory().setItem(item.getSlot(), items.get(item));
                        }
                    } else if (item.isParty()) {

                        player.getInventory().setItem(item.getSlot(), items.get(item));
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

    public HotbarItem fromItemStack(ItemStack itemStack) {
        for (Map.Entry<HotbarItem, ItemStack> entry : getItems().entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(itemStack)) {
                return entry.getKey();
            }
        }

        return null;
    }
}
