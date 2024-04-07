package me.lrxh.practice.util.menu.pagination;

import lombok.AllArgsConstructor;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@AllArgsConstructor
public class PageButton extends Button {

    private int mod;
    private PaginatedMenu menu;

    @Override
    public ItemStack getButtonItem(Player player) {
        if (this.mod > 0) {
            if (hasNext(player)) {
                return new ItemBuilder(Material.REDSTONE_TORCH_ON)
                        .name(ChatColor.GREEN + "Next Page")
                        .lore(Arrays.asList(
                                ChatColor.YELLOW + "Click here to jump",
                                ChatColor.YELLOW + "to the next page."
                        ), player)
                        .clearFlags()
                        .build();
            } else {
                return new ItemBuilder(Material.LEVER)
                        .name(ChatColor.GRAY + "Next Page")
                        .lore(Arrays.asList(
                                ChatColor.YELLOW + "There is no available",
                                ChatColor.YELLOW + "next page."
                        ), player)
                        .clearFlags()
                        .build();
            }
        } else {
            if (hasPrevious(player)) {
                return new ItemBuilder(Material.REDSTONE_TORCH_ON)
                        .name(ChatColor.GREEN + "Previous Page")
                        .lore(Arrays.asList(
                                ChatColor.YELLOW + "Click here to jump",
                                ChatColor.YELLOW + "to the previous page."
                        ), player)
                        .clearFlags()
                        .build();
            } else {
                return new ItemBuilder(Material.LEVER)
                        .name(ChatColor.GRAY + "Previous Page")
                        .lore(Arrays.asList(
                                ChatColor.YELLOW + "There is no available",
                                ChatColor.YELLOW + "previous page."
                        ), player)
                        .clearFlags()
                        .build();
            }
        }
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (this.mod > 0) {
            if (hasNext(player)) {
                this.menu.modPage(player, this.mod);
                Button.playNeutral(player);
            } else {
                Button.playFail(player);
            }
        } else {
            if (hasPrevious(player)) {
                this.menu.modPage(player, this.mod);
                Button.playNeutral(player);
            } else {
                Button.playFail(player);
            }
        }
    }

    private boolean hasNext(Player player) {
        int pg = this.menu.getPage() + this.mod;
        return this.menu.getPages(player) >= pg;
    }

    private boolean hasPrevious(Player player) {
        int pg = this.menu.getPage() + this.mod;
        return pg > 0;
    }

}
