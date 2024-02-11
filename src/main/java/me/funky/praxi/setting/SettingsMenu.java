package me.funky.praxi.setting;

import me.funky.praxi.Locale;
import me.funky.praxi.Praxi;
import me.funky.praxi.profile.KillEffects;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.visibility.VisibilityLogic;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.assemble.AssembleBoard;
import me.funky.praxi.util.assemble.events.AssembleBoardCreateEvent;
import me.funky.praxi.util.assemble.events.AssembleBoardDestroyEvent;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingsMenu extends Menu {
    public SettingsMenu() {
        this.setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("SETTINGS.TITLE");
    }

    @Override
    public int getSize() {
        return Praxi.getInstance().getMenusConfig().getInteger("SETTINGS.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Praxi.getInstance().getMenusConfig().getString("SETTINGS.FILTER"));
    }


    @Override
    public Map<Integer, Button> getButtons(Player player) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        int i = 10;
        for (Settings settings : Settings.values()) {
            buttons.put(i++, new SettingsButton(settings));
        }
        return buttons;
    }

    private static class SettingsButton extends Button {
        private final Settings settings;

        public SettingsButton(Settings settings) {
            this.settings = settings;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ArrayList<String> lore = new ArrayList<>();
            Profile profile = Profile.getByUuid(player.getUniqueId());
            lore.add("&7" + this.settings.getDescription());
            lore.add(" ");
            switch (this.settings) {
                case SHOW_SCOREBOARD: {
                    if (profile.getOptions().showScoreboard()) {
                        lore.add(" &7&l* &aYes");
                        lore.add(" &7&l* &7No");
                        lore.add(" ");
                        lore.add("&aClick to disable");
                        break;
                    }
                    lore.add(" &7&l* &7Yes");
                    lore.add(" &7&l* &cNo");
                    lore.add(" ");
                    lore.add("&aClick to enable");
                    break;
                }
                case PING_RANGE: {
                    if (profile.getOptions().pingRange() == 250) {
                        lore.add(" &7&l* &r&aUnrestricted");
                        lore.add(" ");
                        lore.add("&aClick to decrease");
                        break;
                    }
                    lore.add(" &7&l* &r&a" + profile.getOptions().pingRange());
                    lore.add(" ");
                    lore.add("&aClick to increase");
                    break;
                }
                case SHOW_LINES: {
                    if (profile.getOptions().scoreboradLines()) {
                        lore.add(" &7&l* &aYes");
                        lore.add(" &7&l* &7No");
                        lore.add(" ");
                        lore.add("&aClick to disable");
                        break;
                    }
                    lore.add(" &7&l* &7Yes");
                    lore.add(" &7&l* &cNo");
                    lore.add(" ");
                    lore.add("&aClick to enable");
                    break;
                }
                case ALLOW_DUELS: {
                    if (profile.getOptions().receiveDuelRequests()) {
                        lore.add(" &7&l* &aYes");
                        lore.add(" &7&l* &7No");
                        lore.add(" ");
                        lore.add("&aClick to disable");
                        break;
                    }
                    lore.add(" &7&l* &7Yes");
                    lore.add(" &7&l* &cNo");
                    lore.add(" ");
                    lore.add("&aClick to enable");
                    break;
                }
                case ALLOW_SPECTATORS: {
                    if (profile.getOptions().allowSpectators()) {
                        lore.add(" &7&l* &aYes");
                        lore.add(" &7&l* &7No");
                        lore.add(" ");
                        lore.add("&aClick to disable");
                        break;
                    }
                    lore.add(" &7&l* &7Yes");
                    lore.add(" &7&l* &cNo");
                    lore.add(" ");
                    lore.add("&aClick to enable");
                    break;
                }
                case SHOW_PLAYERS: {
                    if (profile.getOptions().showPlayers()) {
                        lore.add(" &7&l* &aYes");
                        lore.add(" &7&l* &7No");
                        lore.add(" ");
                        lore.add("&aClick to disable");
                        break;
                    }
                    lore.add(" &7&l* &7Yes");
                    lore.add(" &7&l* &cNo");
                    lore.add(" ");
                    lore.add("&aClick to enable");
                    break;
                }
                case KILL_EFFECTS: {
                    switch (profile.getOptions().killEffect()) {
                        case NONE:
                            lore.add("&7&l* &a" + KillEffects.NONE.getDisplayName());
                            for (KillEffects killEffects : KillEffects.values()) {
                                if (killEffects != KillEffects.NONE) {
                                    lore.add("&7&l* &r&7" + killEffects.getDisplayName());
                                }
                            }
                            break;
                        case LIGHTNING:
                            lore.add("&7&l* &a" + KillEffects.LIGHTNING.getDisplayName());
                            for (KillEffects killEffects : KillEffects.values()) {
                                if (killEffects != KillEffects.LIGHTNING) {
                                    lore.add("&7&l* &r&7" + killEffects.getDisplayName());
                                }
                            }
                            break;
                        case FIREWORKS: {
                            lore.add("&7&l* &a" + KillEffects.FIREWORKS.getDisplayName());
                            for (KillEffects killEffects : KillEffects.values()) {
                                if (killEffects != KillEffects.FIREWORKS) {
                                    lore.add("&7&l* &r&7" + killEffects.getDisplayName());
                                }
                            }
                            break;
                        }
                    }
                    lore.add("");
                    lore.add("&aClick to select");
                    break;
                }
                case THEME: {
                    switch (profile.getOptions().theme()) {
                        case AQUA:
                            lore.add("&7&l* &a" + Colors.AQUA.getName());
                            for (Colors colors : Colors.values()) {
                                if (colors != Colors.AQUA) {
                                    lore.add("&7&l* &r&7" + colors.getName());
                                }
                            }
                            break;
                        case ORANGE:
                            lore.add("&7&l* &a" + Colors.ORANGE.getName());
                            for (Colors colors : Colors.values()) {
                                if (colors != Colors.ORANGE) {
                                    lore.add("&7&l* &r&7" + colors.getName());
                                }
                            }
                            break;
                        case PINK:
                            lore.add("&7&l* &a" + Colors.PINK.getName());
                            for (Colors colors : Colors.values()) {
                                if (colors != Colors.PINK) {
                                    lore.add("&7&l* &r&7" + colors.getName());
                                }
                            }
                            break;
                        case RED:
                            lore.add("&7&l* &a" + Colors.RED.getName());
                            for (Colors colors : Colors.values()) {
                                if (colors != Colors.RED) {
                                    lore.add("&7&l* &r&7" + colors.getName());
                                }
                            }
                            break;
                        case YELLOW:
                            lore.add("&7&l* &a" + Colors.YELLOW.getName());
                            for (Colors colors : Colors.values()) {
                                if (colors != Colors.YELLOW) {
                                    lore.add("&7&l* &r&7" + colors.getName());
                                }
                            }
                            break;
                    }
                    lore.add("");
                    lore.add("&aClick to select");
                    break;
                }
            }
            return new ItemBuilder(this.settings.getMaterial()).name(Praxi.getInstance().getMenusConfig().getString("SETTINGS.SETTING-NAME").replace("<settings>", settings.getName())).lore(lore).clearEnchantments().clearFlags().clearFlags().build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            switch (this.settings) {
                case SHOW_SCOREBOARD: {
                    showScoreboard(profile, !profile.getOptions().showScoreboard());
                    if (profile.getOptions().showScoreboard()) {
                        player.sendMessage(Locale.OPTIONS_SCOREBOARD_ENABLED.format(player));
                    } else {
                        player.sendMessage(Locale.OPTIONS_SCOREBOARD_DISABLED.format(player));
                    }
                    break;
                }
                case PING_RANGE: {
                    int ping = profile.getOptions().pingRange();
                    if (ping == 250) {
                        profile.getOptions().pingRange(10);
                    } else {
                        profile.getOptions().pingRange(profile.getOptions().pingRange() + 10);
                    }
                    break;
                }
                case SHOW_LINES: {
                    profile.getOptions().scoreboradLines(!profile.getOptions().scoreboradLines());
                    if (profile.getOptions().scoreboradLines()) {
                        player.sendMessage(Locale.OPTIONS_SCOREBOARD_LINES_ENABLED.format(player));
                    } else {
                        player.sendMessage(Locale.OPTIONS_SCOREBOARD_LINES_DISABLED.format(player));
                    }
                    break;
                }
                case ALLOW_DUELS: {
                    profile.getOptions().receiveDuelRequests(!profile.getOptions().receiveDuelRequests());
                    if (profile.getOptions().receiveDuelRequests()) {
                        player.sendMessage(Locale.OPTIONS_RECEIVE_DUEL_REQUESTS_ENABLED.format(player));
                    } else {
                        player.sendMessage(Locale.OPTIONS_RECEIVE_DUEL_REQUESTS_DISABLED.format(player));
                    }
                    break;
                }
                case ALLOW_SPECTATORS: {
                    profile.getOptions().allowSpectators(!profile.getOptions().allowSpectators());
                    if (profile.getOptions().allowSpectators()) {
                        player.sendMessage(Locale.OPTIONS_SPECTATORS_ENABLED.format(player));
                    } else {
                        player.sendMessage(Locale.OPTIONS_SPECTATORS_DISABLED.format(player));
                    }
                    break;
                }
                case SHOW_PLAYERS: {
                    profile.getOptions().showPlayers(!profile.getOptions().showPlayers());
                    if (profile.getOptions().showPlayers()) {
                        player.sendMessage(Locale.OPTIONS_SHOW_PLAYERS_ENABLED.format(player));
                    } else {
                        player.sendMessage(Locale.OPTIONS_SHOW_PLAYERS_DISABLED.format(player));
                    }
                    VisibilityLogic.handle(player);
                    break;
                }
                case KILL_EFFECTS: {
                    if (!player.hasPermission("praxi.killeffect." + profile.getOptions().killEffect().getDisplayName())) {
                        player.sendMessage(CC.translate("&cYou don't have permission to use this kill effect"));
                        break;
                    }
                    switch (profile.getOptions().killEffect()) {

                        case NONE:
                            profile.getOptions().killEffect(KillEffects.LIGHTNING);
                            break;
                        case LIGHTNING:
                            profile.getOptions().killEffect(KillEffects.FIREWORKS);

                            break;
                        case FIREWORKS: {
                            profile.getOptions().killEffect(KillEffects.NONE);
                            break;
                        }
                    }
                    player.sendMessage(Locale.OPTIONS_KILLEFFECT_SELECT.format(player, profile.getOptions().killEffect().getDisplayName()));
                    break;
                }
                case THEME: {
                    if (!player.hasPermission("praxi.options.theme.selector")) {
                        player.sendMessage(CC.translate("&cYou don't have permission to use the theme selector"));
                        break;
                    }
                    switch (profile.getOptions().theme()) {
                        case ORANGE:
                            profile.getOptions().theme(Colors.AQUA);
                            break;
                        case AQUA:
                            profile.getOptions().theme(Colors.RED);
                            break;
                        case RED:
                            profile.getOptions().theme(Colors.YELLOW);
                            break;
                        case YELLOW:
                            profile.getOptions().theme(Colors.PINK);
                            break;
                        case PINK:
                            profile.getOptions().theme(Colors.ORANGE);
                            break;
                    }
                    player.sendMessage(Locale.OPTIONS_THEME_SELECT.format(player, profile.getOptions().theme().getName()));
                }
            }
            new SettingsMenu().openMenu(player);
            player.updateInventory();
        }

        public void showScoreboard(Profile profile, boolean b) {
            profile.getOptions().showScoreboard(!profile.getOptions().showScoreboard());

            profile.getOptions().showScoreboard(b);
            if (profile.getOptions().showScoreboard()) {
                if (Praxi.getInstance().getAssemble().isCallEvents()) {
                    AssembleBoardCreateEvent createEvent = new AssembleBoardCreateEvent(profile.getPlayer());

                    Bukkit.getPluginManager().callEvent(createEvent);
                    if (createEvent.isCancelled()) {
                        return;
                    }
                }

                Praxi.getInstance().getAssemble().getBoards().put(profile.getPlayer().getUniqueId(),
                        new AssembleBoard(profile.getPlayer(), Praxi.getInstance().getAssemble()));
            } else {
                if (Praxi.getInstance().getAssemble().isCallEvents()) {
                    AssembleBoardDestroyEvent destroyEvent = new AssembleBoardDestroyEvent(profile.getPlayer());

                    Bukkit.getPluginManager().callEvent(destroyEvent);
                    if (destroyEvent.isCancelled()) {
                        return;
                    }
                }

                Praxi.getInstance().getAssemble().getBoards().get(profile.getPlayer().getUniqueId()).getObjective().unregister();
                Praxi.getInstance().getAssemble().getBoards().remove(profile.getPlayer().getUniqueId());
            }
        }
    }
}
 