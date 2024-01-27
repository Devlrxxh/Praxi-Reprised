package me.funky.praxi.profile.hotbar;

import lombok.Getter;
import me.funky.praxi.Praxi;
import me.funky.praxi.event.game.EventGame;
import me.funky.praxi.event.game.EventGameState;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.PlayerUtil;
import net.minecraft.server.v1_8_R3.Item;
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

                    itemStacks[getSlot(HotbarItem.QUEUE_JOIN_UNRANKED)] = items.get(HotbarItem.QUEUE_JOIN_UNRANKED);
                    itemStacks[getSlot(HotbarItem.QUEUE_JOIN_RANKED)] = items.get(HotbarItem.QUEUE_JOIN_RANKED);

                    if (activeRematch && activeEvent) {
                        if (profile.getRematchData().isReceive()) {
                            itemStacks[getSlot(HotbarItem.REMATCH_ACCEPT)] = items.get(HotbarItem.REMATCH_ACCEPT);
                        } else {
                            itemStacks[getSlot(HotbarItem.REMATCH_ACCEPT)] = items.get(HotbarItem.REMATCH_REQUEST);
                        }

                        itemStacks[getSlot(HotbarItem.EVENT_JOIN)] = items.get(HotbarItem.EVENT_JOIN);
                        itemStacks[getSlot(HotbarItem.PARTY_CREATE)] = items.get(HotbarItem.PARTY_CREATE);
                    } else if (activeRematch) {
                        if (profile.getRematchData().isReceive()) {
                            itemStacks[getSlot(HotbarItem.REMATCH_ACCEPT)] = items.get(HotbarItem.REMATCH_ACCEPT);
                        } else {
                            itemStacks[getSlot(HotbarItem.REMATCH_REQUEST)] = items.get(HotbarItem.REMATCH_REQUEST);
                        }

                        itemStacks[getSlot(HotbarItem.PARTY_CREATE)] = items.get(HotbarItem.PARTY_CREATE);
                    } else if (activeEvent) {
                        itemStacks[getSlot(HotbarItem.EVENT_JOIN)] = items.get(HotbarItem.EVENT_JOIN);
                        itemStacks[getSlot(HotbarItem.PARTY_CREATE)] = items.get(HotbarItem.PARTY_CREATE);
                    } else {
                        itemStacks[getSlot(HotbarItem.PARTY_CREATE)] = items.get(HotbarItem.PARTY_CREATE);
                    }
                } else {
                    if (profile.getParty().getLeader().getUniqueId().equals(profile.getUuid())) {
                        itemStacks[getSlot(HotbarItem.PARTY_EVENTS)] = items.get(HotbarItem.PARTY_EVENTS);
                        itemStacks[getSlot(HotbarItem.PARTY_INFORMATION)] = items.get(HotbarItem.PARTY_INFORMATION);
                        itemStacks[getSlot(HotbarItem.OTHER_PARTIES)] = items.get(HotbarItem.OTHER_PARTIES);
                        itemStacks[getSlot(HotbarItem.PARTY_DISBAND)] = items.get(HotbarItem.PARTY_DISBAND);
                    } else {
                        itemStacks[getSlot(HotbarItem.PARTY_INFORMATION)] = items.get(HotbarItem.PARTY_INFORMATION);
                        itemStacks[getSlot(HotbarItem.OTHER_PARTIES)] = items.get(HotbarItem.OTHER_PARTIES);
                        itemStacks[getSlot(HotbarItem.PARTY_LEAVE)] = items.get(HotbarItem.PARTY_LEAVE);
                    }
                }

                itemStacks[getSlot(HotbarItem.KIT_EDITOR)] = items.get(HotbarItem.KIT_EDITOR);
            }
            break;
            case QUEUEING: {
                itemStacks[getSlot(HotbarItem.QUEUE_LEAVE)] = items.get(HotbarItem.QUEUE_LEAVE);
            }
            break;
            case SPECTATING:
            case FIGHTING: {
                itemStacks[getSlot(HotbarItem.SPECTATE_STOP)] = items.get(HotbarItem.SPECTATE_STOP);
            }
            break;
            case EVENT: {
                itemStacks[getSlot(HotbarItem.EVENT_LEAVE)] = items.get(HotbarItem.EVENT_LEAVE);
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

    public static int getSlot(HotbarItem hotbarItem){
        return Praxi.getInstance().getMainConfig().getInteger("HOTBAR_ITEMS." + hotbarItem.toString() + ".SLOT");
    }

}
