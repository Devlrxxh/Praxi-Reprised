package me.lrxh.practice.setting;

import lombok.Getter;
import org.bukkit.ChatColor;

@Getter
public enum Colors {
    ORANGE("Orange", ChatColor.GOLD),
    AQUA("Aqua", ChatColor.AQUA),
    RED("Red", ChatColor.RED),
    YELLOW("Yellow", ChatColor.YELLOW),
    PINK("Pink", ChatColor.LIGHT_PURPLE);


    private final String name;
    private final ChatColor color;

    Colors(String name, ChatColor color) {
        this.name = name;
        this.color = color;
    }
}
