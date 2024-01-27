package me.funky.praxi.adapter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface Core {

    String getName(UUID uuid);
    String getPrefix(UUID uuid);
    String getSuffix(UUID uuid);
    ChatColor getColor(UUID uuid);
    String getColoredName(UUID uuid);
    String getTag(Player player);
    String getRealName(Player player);
    int getWeight(UUID uuid);

}
