package me.funky.praxi.util.menu;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.Praxi;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class Menu {

    public static Map<String, Menu> currentlyOpenedMenus = new HashMap<>();

    protected Praxi praxi = Praxi.getInstance();
    private Map<Integer, Button> buttons = new HashMap<>();
    private boolean autoUpdate = false;
    private boolean updateAfterClick = true;
    private boolean closedByMenu = false;
    private ItemStack fillerType;
    private int size = 9;
    //private boolean border = false;
    //private boolean fill = false;
    private Filters filter;

    {
        fillerType = (new ItemStack(Material.valueOf(Praxi.getInstance().getMenusConfig().getString("FILTER.MATERIAL")), 1
                , (short) Praxi.getInstance().getMenusConfig().getInteger("FILTER.DURABILITY")));
        ItemMeta fillerMeta = fillerType.getItemMeta();

        if (fillerMeta != null) {
            fillerMeta.setDisplayName(Praxi.getInstance().getMenusConfig().getString("FILTER.NAME"));
            fillerType.setItemMeta(fillerMeta);
        }
    }

    private void fillBorder(Inventory inventory) {
        int size = inventory.getSize();

        if (size < 9) return;

        ItemStack fillerItem = this.fillerType;

        for (int i = 1; i <= 7 && size >= 18; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem);
                inventory.setItem(size - i - 1, fillerItem);
            }
        }

        for (int i = 1; i <= 2 && size >= 18; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i * 9, fillerItem);
                inventory.setItem(i * 9 + 8, fillerItem);
            }
        }
        inventory.setItem(0, fillerItem);
        inventory.setItem(8, fillerItem);
        inventory.setItem(size - 9, fillerItem);
        inventory.setItem(size - 1, fillerItem);
    }

    private void fill(Inventory inventory) {
        int size = inventory.getSize();

        for (int pos = 0; pos < size; pos++) {
            if (inventory.getItem(pos) == null)
                inventory.setItem(pos, fillerType);
        }
    }


    private ItemStack createItemStack(Player player, Button button) {
        ItemStack item = button.getButtonItem(player);

        if (item.getType() != Material.SKULL_ITEM) {
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasDisplayName()) {
                meta.setDisplayName(meta.getDisplayName() + "§b§c§d§e");
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public void openMenu(final Player player) {
        this.buttons = this.getButtons(player);

        Menu previousMenu = Menu.currentlyOpenedMenus.get(player.getName());
        Inventory inventory = null;
        int size = this.getSize() == -1 ? this.size(this.buttons) : this.getSize();
        if (getFilter() != null) {
            this.filter = getFilter();
        }
        boolean update = false;
        String title = CC.translate(this.getTitle(player));

        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        if (player.getOpenInventory() != null) {
            if (previousMenu == null) {
                player.closeInventory();
            } else {
                int previousSize = player.getOpenInventory().getTopInventory().getSize();

                if (previousSize == size && player.getOpenInventory().getTopInventory().getTitle().equals(title)) {
                    inventory = player.getOpenInventory().getTopInventory();
                    update = true;
                } else {
                    previousMenu.setClosedByMenu(true);
                    player.closeInventory();
                }
            }
        }

        if (inventory == null) {
            inventory = Bukkit.createInventory(player, size, title);
        }

        inventory.setContents(new ItemStack[inventory.getSize()]);

        currentlyOpenedMenus.put(player.getName(), this);

        for (Map.Entry<Integer, Button> buttonEntry : this.buttons.entrySet()) {
            int slot = buttonEntry.getKey();
            if (filter != Filters.NONE) {
                if (slot % 9 == 0 || slot % 9 == 8) {
                    slot += 2;
                }
            }
            inventory.setItem(slot, createItemStack(player, buttonEntry.getValue()));
        }
        switch (filter) {
            case BORDER:
                fillBorder(inventory);
                break;
            case FILL:
                fill(inventory);
                break;
        }


        if (update) {
            player.updateInventory();
        } else {
            player.openInventory(inventory);
        }

        this.onOpen(player);
        this.setClosedByMenu(false);
    }

    public int size(Map<Integer, Button> buttons) {
        int highest = 0;

        for (int buttonValue : buttons.keySet()) {
            if (buttonValue > highest) {
                highest = buttonValue;
            }
        }

        return (int) (Math.ceil((highest + 1) / 9D) * 9D);
    }

    public int getSize() {
        return -1;
    }

    public Filters getFilter() {
        return Filters.NONE;
    }


    public int getSlot(int x, int y) {
        return ((9 * y) + x);
    }

    public abstract String getTitle(Player player);

    public abstract Map<Integer, Button> getButtons(Player player);

    public void onOpen(Player player) {
    }

    public void onClose(Player player) {
    }

}
