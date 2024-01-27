package me.funky.praxi.adapter.impl;

import me.funky.praxi.adapter.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Default implements Core {

    @Override
    public String getName(UUID uuid) {
        return "Default";
    }

    @Override
    public String getPrefix(UUID uuid) {
        return "Default";
    }

    @Override
    public String getSuffix(UUID uuid) {
        return "Default";
    }

    @Override
    public ChatColor getColor(UUID uuid) {
        return ChatColor.GREEN;
    }

    @Override
    public String getColoredName(UUID uuid) {
        return ChatColor.GREEN + Bukkit.getPlayer(uuid).getName();
    }

    @Override
    public String getRealName(Player player) {
        return null;
    }

    @Override
    public String getTag(Player player) {
        return "";
    }

    @Override
    public int getWeight(UUID uuid) {
        return 0;
    }
}
