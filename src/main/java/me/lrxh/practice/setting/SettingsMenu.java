package me.lrxh.practice.setting;

import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.KillEffects;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.Themes;
import me.lrxh.practice.profile.Times;
import me.lrxh.practice.profile.visibility.VisibilityLogic;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.assemble.AssembleBoard;
import me.lrxh.practice.util.assemble.events.AssembleBoardCreateEvent;
import me.lrxh.practice.util.assemble.events.AssembleBoardDestroyEvent;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.filters.Filters;
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
        return Practice.getInstance().getMenusConfig().getString("SETTINGS.TITLE");
    }

    @Override
    public int getSize() {
        return Practice.getInstance().getMenusConfig().getInteger("SETTINGS.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("SETTINGS.FILTER"));
    }

    public boolean resetCursor() {
        return false;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        int i = 9;
        for (Settings settings : Settings.values()) {
            buttons.put(i += 1, new SettingsButton(settings));
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
                    lore.add(profile.getOptions().showScoreboard() ? " &7&l▶ &aYes" : " &7&l▶ &7Yes");
                    lore.add(!profile.getOptions().showScoreboard() ? " &7&l▶ &cNo" : " &7&l▶ &7No");
                    lore.add(" ");
                    lore.add(!profile.getOptions().showScoreboard() ? "&aClick to enable" : "&aClick to disable");
                    break;
                }
                case MENU_SOUNDS: {
                    lore.add(profile.getOptions().menuSounds() ? " &7&l▶ &aYes" : " &7&l▶ &7Yes");
                    lore.add(!profile.getOptions().menuSounds() ? " &7&l▶ &cNo" : " &7&l▶ &7No");
                    lore.add(" ");
                    lore.add(!profile.getOptions().menuSounds() ? "&aClick to enable" : "&aClick to disable");
                    break;
                }
                case TIME_CHANGE: {
                    lore.add(profile.getOptions().time().equals(Times.DAY) ? " &7&l▶ &aDay" : " &7&l▶ &7Day");
                    lore.add(profile.getOptions().time().equals(Times.NIGHT) ? " &7&l▶ &aNight" : " &7&l▶ &7Night");
                    lore.add(profile.getOptions().time().equals(Times.SUNRISE) ? " &7&l▶ &aSunrise" : " &7&l▶ &7Sunrise");
                    lore.add(profile.getOptions().time().equals(Times.SUNSET) ? " &7&l▶ &aSunset" : " &7&l▶ &7Sunset");
                    lore.add("");
                    lore.add("&aClick to select");
                    break;
                }
                case PING_RANGE: {
                    lore.add(profile.getOptions().pingRange() == 250 ? " &7&l▶ &r&aUnrestricted" : " &7&l▶ &r&a" + profile.getOptions().pingRange());
                    lore.add(" ");
                    lore.add(profile.getOptions().pingRange() == 250 ? "&aClick to decrease" : "&aClick to increase");
                    break;
                }
                case ALLOW_DUELS: {
                    lore.add(profile.getOptions().receiveDuelRequests() ? " &7&l▶ &aYes" : " &7&l▶ &7Yes");
                    lore.add(!profile.getOptions().receiveDuelRequests() ? " &7&l▶ &cNo" : " &7&l▶ &7No");
                    lore.add(" ");
                    lore.add(!profile.getOptions().receiveDuelRequests() ? "&aClick to enable" : "&aClick to disable");
                    break;
                }
                case ALLOW_SPECTATORS: {
                    lore.add(profile.getOptions().allowSpectators() ? " &7&l▶ &aYes" : " &7&l▶ &7Yes");
                    lore.add(!profile.getOptions().allowSpectators() ? " &7&l▶ &cNo" : " &7&l▶ &7No");
                    lore.add(" ");
                    lore.add(!profile.getOptions().allowSpectators() ? "&aClick to enable" : "&aClick to disable");
                    break;
                }
                case SHOW_PLAYERS: {
                    lore.add(profile.getOptions().showPlayers() ? " &7&l▶ &aYes" : " &7&l▶ &7Yes");
                    lore.add(!profile.getOptions().showPlayers() ? " &7&l▶ &cNo" : " &7&l▶ &7No");
                    lore.add(" ");
                    lore.add(!profile.getOptions().showPlayers() ? "&aClick to enable" : "&aClick to disable");
                    break;
                }
                case KILL_EFFECTS: {
                    lore.add(profile.getOptions().killEffect().equals(KillEffects.NONE) ? " &7&l▶ &aNone" : " &7&l▶ &7None");
                    lore.add(profile.getOptions().killEffect().equals(KillEffects.LIGHTNING) ? " &7&l▶ &aLightning" : " &7&l▶ &7Lightning");
                    lore.add(profile.getOptions().killEffect().equals(KillEffects.FIREWORKS) ? " &7&l▶ &aFireworks" : " &7&l▶ &7Fireworks");
                    lore.add("");
                    lore.add("&aClick to select");
                    break;
                }
                case THEME: {
                    lore.add(profile.getOptions().theme().equals(Themes.AQUA) ? " &7&l▶ &aAqua" : " &7&l▶ &7Aqua");
                    lore.add(profile.getOptions().theme().equals(Themes.RED) ? " &7&l▶ &aRed" : " &7&l▶ &7Red");
                    lore.add(profile.getOptions().theme().equals(Themes.YELLOW) ? " &7&l▶ &aYellow" : " &7&l▶ &7Yellow");
                    lore.add(profile.getOptions().theme().equals(Themes.PINK) ? " &7&l▶ &aPink" : " &7&l▶ &7Pink");
                    lore.add(profile.getOptions().theme().equals(Themes.ORANGE) ? " &7&l▶ &aOrange" : " &7&l▶ &7Orange");
                    lore.add("");
                    lore.add("&aClick to select");
                    break;
                }
            }
            return new ItemBuilder(this.settings.getMaterial()).name(Practice.getInstance().getMenusConfig().getString("SETTINGS.SETTING-NAME").replace("<settings>", settings.getName())).lore(lore, player).clearEnchantments().clearFlags().clearFlags().build();
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
                case MENU_SOUNDS: {
                    profile.getOptions().menuSounds(!profile.getOptions().menuSounds());
                    if (profile.getOptions().menuSounds()) {
                        player.sendMessage(Locale.OPTIONS_MENU_SOUNDS_ENABLED.format(player));
                    } else {
                        player.sendMessage(Locale.OPTIONS_MENU_SOUNDS_DISABLED.format(player));
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
                case TIME_CHANGE: {
                    switch (profile.getOptions().time()) {
                        case DAY:
                            profile.getOptions().time(Times.NIGHT);
                            break;
                        case NIGHT:
                            profile.getOptions().time(Times.SUNRISE);
                            break;
                        case SUNRISE:
                            profile.getOptions().time(Times.SUNSET);
                            break;
                        case SUNSET:
                            profile.getOptions().time(Times.DAY);
                            break;
                    }
                    player.setPlayerTime(profile.getOptions().time().getTime(), false);
                    player.sendMessage(Locale.OPTIONS_TIME_SELECT.format(player, profile.getOptions().time().getName()));
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
                    if (!player.hasPermission("practice.killeffect." + profile.getOptions().killEffect().getDisplayName())) {
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
                    if (!player.hasPermission("practice.theme." + profile.getOptions().theme().getName())) {
                        player.sendMessage(CC.translate("&cYou don't have permission to use the theme selector"));
                        break;
                    }
                    switch (profile.getOptions().theme()) {
                        case ORANGE:
                            profile.getOptions().theme(Themes.AQUA);
                            break;
                        case AQUA:
                            profile.getOptions().theme(Themes.RED);
                            break;
                        case RED:
                            profile.getOptions().theme(Themes.YELLOW);
                            break;
                        case YELLOW:
                            profile.getOptions().theme(Themes.PINK);
                            break;
                        case PINK:
                            profile.getOptions().theme(Themes.ORANGE);
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
                if (Practice.getInstance().getAssemble().isCallEvents()) {
                    AssembleBoardCreateEvent createEvent = new AssembleBoardCreateEvent(profile.getPlayer());

                    Bukkit.getPluginManager().callEvent(createEvent);
                    if (createEvent.isCancelled()) {
                        return;
                    }
                }

                Practice.getInstance().getAssemble().getBoards().put(profile.getPlayer().getUniqueId(),
                        new AssembleBoard(profile.getPlayer(), Practice.getInstance().getAssemble()));
            } else {
                if (Practice.getInstance().getAssemble().isCallEvents()) {
                    AssembleBoardDestroyEvent destroyEvent = new AssembleBoardDestroyEvent(profile.getPlayer());

                    Bukkit.getPluginManager().callEvent(destroyEvent);
                    if (destroyEvent.isCancelled()) {
                        return;
                    }
                }

                Practice.getInstance().getAssemble().getBoards().get(profile.getPlayer().getUniqueId()).getObjective().unregister();
                Practice.getInstance().getAssemble().getBoards().remove(profile.getPlayer().getUniqueId());
            }
        }
    }
}
 