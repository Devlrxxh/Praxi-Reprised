package me.funky.praxi.setting;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum Settings {
    SHOW_SCOREBOARD("Show Scoreboard", Material.EYE_OF_ENDER, "Show or Hide Scoreboard."),
    ALLOW_SPECTATORS("Allow Spectators", Material.EGG, "Show or Hide Players."),
    ALLOW_DUELS("Allow Duels", Material.DIAMOND_SWORD, "Allow Duel Requests.");

    private final String name;
    private final Material material;
    private final String description;

    private Settings(String name, Material material, String description) {
        this.name = name;
        this.material = material;
        this.description = description;
    }
}
 