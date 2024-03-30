package me.lrxh.practice.setting;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum Settings {

    SHOW_PLAYERS("Toggle Players Visibility", Material.COMPASS, "Show or Hide players."),
    SHOW_SCOREBOARD("Toggle Scoreboard", Material.EYE_OF_ENDER, "Show or Hide Scoreboard."),
    ALLOW_SPECTATORS("Toggle Spectators", Material.EGG, "Allow players to spectate."),
    ALLOW_DUELS("Toggle Duels", Material.DIAMOND_SWORD, "Allow Duel Requests."),
    KILL_EFFECTS("Kill Effects", Material.DIAMOND_AXE, "Select Kill Effect."),
    THEME("Select Theme", Material.BOOK, "Select Color Theme."),
    PING_RANGE("Ping Range", Material.STICK, "Change Ping Range."),
    TIME_CHANGE("Change Time", Material.WATCH, "Change Ping Range."),
    MENU_SOUNDS("Menu Sounds", Material.REDSTONE_COMPARATOR, "Toggle Menu Sounds.");

    private final String name;
    private final Material material;
    private final String description;

    Settings(String name, Material material, String description) {
        this.name = name;
        this.material = material;
        this.description = description;
    }
}
