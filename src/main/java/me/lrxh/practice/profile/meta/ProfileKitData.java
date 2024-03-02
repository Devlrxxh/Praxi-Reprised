package me.lrxh.practice.profile.meta;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.Practice;
import me.lrxh.practice.kit.KitLoadout;
import me.lrxh.practice.profile.hotbar.HotbarItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ProfileKitData {

    private int elo = 1000;
    private int won;
    private int lost;
    private KitLoadout[] loadouts = new KitLoadout[4];


    public void incrementWon() {
        this.won++;
    }

    public void incrementLost() {
        this.lost++;
    }

    public KitLoadout getLoadout(int index) {
        return loadouts[index];
    }

    public void replaceKit(int index, KitLoadout loadout) {
        loadouts[index] = loadout;
    }

    public void deleteKit(KitLoadout loadout) {
        for (int i = 0; i < 4; i++) {
            if (loadouts[i] != null && loadouts[i].equals(loadout)) {
                loadouts[i] = null;
                break;
            }
        }
    }

    public int getKitCount() {
        int i = 0;

        for (KitLoadout loadout : loadouts) {
            if (loadout != null) {
                i++;
            }
        }

        return i;
    }

    public void giveBooks(Player player) {
        List<KitLoadout> loadouts = new ArrayList<>();

        for (KitLoadout loadout : this.loadouts) {
            if (loadout != null) {
                loadouts.add(loadout);
            }
        }

        ItemStack defaultKitItemStack = Practice.getInstance().getHotbar().getItems().get(HotbarItem.KIT_SELECTION).clone();


        ItemMeta defaultKitItemMeta = defaultKitItemStack.getItemMeta();
        defaultKitItemMeta.setDisplayName(defaultKitItemMeta.getDisplayName()
                .replace("%KIT%", "Default"));
        defaultKitItemStack.setItemMeta(defaultKitItemMeta);

        if (loadouts.isEmpty()) {
            player.getInventory().setItem(0, defaultKitItemStack);
        } else {
            player.getInventory().setItem(8, defaultKitItemStack);

            for (KitLoadout loadout : loadouts) {
                ItemStack itemStack = Practice.getInstance().getHotbar().getItems().get(HotbarItem.KIT_SELECTION).clone();
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(itemMeta.getDisplayName().replace("%KIT%", loadout.getCustomName()));
                itemStack.setItemMeta(itemMeta);

                player.getInventory().addItem(itemStack);
            }
        }

        player.updateInventory();
    }

}
