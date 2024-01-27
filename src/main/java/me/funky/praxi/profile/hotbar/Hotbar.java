package me.funky.praxi.profile.hotbar;

import lombok.Getter;
import me.funky.praxi.Praxi;
import me.funky.praxi.event.game.EventGame;
import me.funky.praxi.event.game.EventGameState;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Hotbar {

    @Getter
    private static final Map<HotbarItem, ItemStack> items = new HashMap<>();

    public static void init() {
        FileConfiguration config = Praxi.getInstance().getMainConfig().getConfiguration();

        for (HotbarItem hotbarItem : HotbarItem.values()) {
            try {
                String path = "HOTBAR_ITEMS." + hotbarItem.name() + ".";

                ItemBuilder builder = new ItemBuilder(Material.valueOf(config.getString(path + "MATERIAL")));
                builder.durability(config.getInt(path + "DURABILITY"));
                builder.name(config.getString(path + "NAME"));
                builder.lore(config.getStringList(path + "LORE"));

                items.put(hotbarItem, builder.build());
            } catch (Exception e) {
                System.out.println("Failed to parse item " + hotbarItem.name());
            }
        }

        Map<HotbarItem, String> dynamicContent = new HashMap<>();
        dynamicContent.put(HotbarItem.MAP_SELECTION, "%MAP%");
        dynamicContent.put(HotbarItem.KIT_SELECTION, "%KIT%");

        for (Map.Entry<HotbarItem, String> entry : dynamicContent.entrySet()) {
            String voteName = Hotbar.getItems().get(entry.getKey()).getItemMeta().getDisplayName();
            String[] nameSplit = voteName.split(entry.getValue());

            entry.getKey().setPattern(
                    Pattern.compile("(" + nameSplit[0] + ")(.*)(" + (nameSplit.length > 1 ? nameSplit[1] : "") + ")"));
        }
    }

    public static void giveHotbarItems(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        ItemStack[] itemStacks = new ItemStack[9];
        Arrays.fill(itemStacks, null);

        boolean activeRematch = profile.getRematchData() != null;
        boolean activeEvent = EventGame.getActiveGame() != null &&
                EventGame.getActiveGame().getGameState() == EventGameState.WAITING_FOR_PLAYERS;

        switch (profile.getState()) {
            case LOBBY: {
                if (profile.getParty() == null) {
                    itemStacks[0] = items.get(HotbarItem.QUEUE_JOIN_UNRANKED);
                    itemStacks[1] = items.get(HotbarItem.QUEUE_JOIN_RANKED);

                    if (activeRematch && activeEvent) {
                        if (profile.getRematchData().isReceive()) {
                            itemStacks[2] = items.get(HotbarItem.REMATCH_ACCEPT);
                        } else {
                            itemStacks[2] = items.get(HotbarItem.REMATCH_REQUEST);
                        }

                        itemStacks[3] = items.get(HotbarItem.EVENT_JOIN);
                        itemStacks[5] = items.get(HotbarItem.PARTY_CREATE);
                    } else if (activeRematch) {
                        if (profile.getRematchData().isReceive()) {
                            itemStacks[2] = items.get(HotbarItem.REMATCH_ACCEPT);
                        } else {
                            itemStacks[2] = items.get(HotbarItem.REMATCH_REQUEST);
                        }

                        itemStacks[4] = items.get(HotbarItem.PARTY_CREATE);
                    } else if (activeEvent) {
                        itemStacks[3] = items.get(HotbarItem.EVENT_JOIN);
                        itemStacks[5] = items.get(HotbarItem.PARTY_CREATE);
                    } else {
                        itemStacks[4] = items.get(HotbarItem.PARTY_CREATE);
                    }
                } else {
                    if (profile.getParty().getLeader().getUniqueId().equals(profile.getUuid())) {
                        itemStacks[0] = items.get(HotbarItem.PARTY_EVENTS);
                        itemStacks[2] = items.get(HotbarItem.PARTY_INFORMATION);
                        itemStacks[4] = items.get(HotbarItem.OTHER_PARTIES);
                        itemStacks[6] = items.get(HotbarItem.PARTY_DISBAND);
                    } else {
                        itemStacks[0] = items.get(HotbarItem.PARTY_INFORMATION);
                        itemStacks[3] = items.get(HotbarItem.OTHER_PARTIES);
                        itemStacks[5] = items.get(HotbarItem.PARTY_LEAVE);
                    }
                }

                itemStacks[8] = items.get(HotbarItem.KIT_EDITOR);
            }
            break;
            case QUEUEING: {
                itemStacks[0] = items.get(HotbarItem.QUEUE_LEAVE);
            }
            break;
            case SPECTATING: {
                itemStacks[0] = items.get(HotbarItem.SPECTATE_STOP);
            }
            break;
            case EVENT: {
                itemStacks[8] = items.get(HotbarItem.EVENT_LEAVE);
            }
            break;
            case FIGHTING: {
                itemStacks[8] = items.get(HotbarItem.SPECTATE_STOP);
            }
            break;
        }

        PlayerUtil.reset(player);

        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, itemStacks[i]);
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
