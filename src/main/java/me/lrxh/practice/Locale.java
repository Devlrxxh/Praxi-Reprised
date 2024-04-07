package me.lrxh.practice;

import lombok.AllArgsConstructor;
import me.lrxh.practice.util.PlaceholderUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public enum Locale {
    KIT_EDITOR_START_RENAMING("KIT_EDITOR.START_RENAMING"),
    KIT_EDITOR_RENAMED("KIT_EDITOR.RENAMED"),
    KIT_EDITOR_NAME_TOO_LONG("KIT_EDITOR.NAME_TOO_LONG"),
    DUEL_SENT("DUEL.SENT"),
    DUEL_SENT_PARTY("DUEL.SENT_PARTY"),
    DUEL_RECEIVED("DUEL.RECEIVED"),
    DUEL_RECEIVED_PARTY("DUEL.RECEIVED_PARTY"),
    DUEL_RECEIVED_HOVER("DUEL.RECEIVED_HOVER"),
    DUEL_RECEIVED_CLICKABLE("DUEL.RECEIVED_CLICKABLE"),
    PARTY_HELP("PARTY.HELP"),
    PARTY_INFORMATION("PARTY.INFORMATION"),
    PARTY_CREATE("PARTY.CREATE"),
    PARTY_DISBAND("PARTY.DISBAND"),
    PARTY_INVITE("PARTY.INVITE"),
    PARTY_INVITE_HOVER("PARTY.INVITE_HOVER"),
    PARTY_INVITE_BROADCAST("PARTY.INVITE_BROADCAST"),
    PARTY_JOIN("PARTY.JOIN"),
    PARTY_LEAVE("PARTY.LEAVE"),
    PARTY_PRIVACY_CHANGE("PARTY.PRIVACY_CHANGE"),
    PARTY_CHAT_PREFIX("PARTY.CHAT_PREFIX"),
    QUEUE_JOIN_UNRANKED("QUEUE.JOIN_UNRANKED"),
    QUEUE_LEAVE_UNRANKED("QUEUE.LEAVE_UNRANKED"),
    QUEUE_JOIN_RANKED("QUEUE.JOIN_RANKED"),
    RANKED_ERROR("QUEUE.RANKED-ERROR-MESSAGE"),
    LEADERBOARD_REFRESH("LEADERBOARD.MESSAGE"),
    PING_YOUR("PING.YOUR"),
    PING_OTHERS("PING.OTHERS"),
    QUEUE_LEAVE_RANKED("QUEUE.LEAVE_RANKED"),
    MATCH_GIVE_KIT("MATCH.GIVE_KIT"),
    MATCH_ENDERPEARL_COOLDOWN("MATCH.ENDERPEARL_COOLDOWN"),
    MATCH_ENDERPEARL_COOLDOWN_EXPIRED("MATCH.ENDERPEARL_COOLDOWN_EXPIRED"),
    MATCH_START_SPECTATING("MATCH.START_SPECTATING"),
    MATCH_START_SPECTATING_RANKED("MATCH.START_SPECTATING_RANKED"),
    MATCH_NOW_SPECTATING("MATCH.NOW_SPECTATING"),
    MATCH_NO_LONGER_SPECTATING("MATCH.NO_LONGER_SPECTATING"),
    MATCH_PLAYING_ARENA("MATCH.PLAYING_ARENA"),
    MATCH_START("MATCH.START"),
    MATCH_BED_BROKEN("MATCH.BED_BROKEN"),
    MATCH_SHOW_REPLAY("MATCH.REPLAY.SHOW_REPLAY"),
    MATCH_SHOW_REPLAY_HOVER("MATCH.REPLAY.RECEIVED_HOVER"),
    MATCH_SHOW_REPLAY_RECEIVED_CLICKABLE("MATCH.REPLAY.RECEIVED_CLICKABLE"),
    REMATCH_SHOW_REPLAY_HOVER("MATCH.REMATCH.RECEIVED_HOVER"),
    REMATCH_SHOW_REPLAY_RECEIVED_CLICKABLE("MATCH.REMATCH.RECEIVED_CLICKABLE"),
    MATCH_START_TIMER("MATCH.START_TIMER"),
    MATCH_RESPAWN_TIMER("MATCH.RESPAWN_TIMER"),
    MATCH_RESPAWNED("MATCH.RESPAWNED"),
    MATCH_STARTED("MATCH.STARTED"),
    MATCH_WARNING("MATCH.WARNING"),
    MATCH_END_DETAILS("MATCH.END_DETAILS"),
    MATCH_END_WINNER_INVENTORY("MATCH.END_WINNER_INVENTORY"),
    MATCH_END_LOSER_INVENTORY("MATCH.END_LOSER_INVENTORY"),
    MATCH_CLICK_TO_VIEW_NAME("MATCH.CLICK_TO_VIEW_NAME"),
    MATCH_CLICK_TO_VIEW_HOVER("MATCH.CLICK_TO_VIEW_HOVER"),
    MATCH_ELO_CHANGES("MATCH.ELO_CHANGES"),
    MATCH_PLAYER_KILLED("MATCH.PLAYER_KILLED"),
    MATCH_PLAYER_FINAL_KILL("MATCH.PLAYER_FINAL_KILL"),
    MATCH_PLAYER_DIED("MATCH.PLAYER_DIED"),
    MATCH_PLAYER_DIED_FINAL("MATCH.PLAYER_DIED_FINAL"),
    REMATCH_SENT_REQUEST("REMATCH.SENT_REQUEST"),
    REMATCH_RECEIVED_REQUEST("REMATCH.RECEIVED_REQUEST"),
    REMATCH_RECEIVED_REQUEST_HOVER("REMATCH.RECEIVED_REQUEST_HOVER"),
    ARROW_DAMAGE_INDICATOR("ARROW_DAMAGE_INDICATOR"),
    OPTIONS_SCOREBOARD_ENABLED("OPTIONS.SCOREBOARD_ENABLED"),
    OPTIONS_KILLEFFECT_SELECT("OPTIONS.KILL_EFFECTS_SELECT"),
    OPTIONS_TIME_SELECT("OPTIONS.TIME_SELECT"),
    OPTIONS_THEME_SELECT("OPTIONS.THEME_SELECT"),
    OPTIONS_SCOREBOARD_DISABLED("OPTIONS.SCOREBOARD_DISABLED"),
    OPTIONS_MENU_SOUNDS_ENABLED("OPTIONS.MENU_SOUNDS_ENABLED"),
    OPTIONS_MENU_SOUNDS_DISABLED("OPTIONS.MENU_SOUNDS_DISABLED"),
    OPTIONS_RECEIVE_DUEL_REQUESTS_ENABLED("OPTIONS.RECEIVE_DUEL_REQUESTS_ENABLED"),
    OPTIONS_RECEIVE_DUEL_REQUESTS_DISABLED("OPTIONS.RECEIVE_DUEL_REQUESTS_DISABLED"),
    OPTIONS_SPECTATORS_ENABLED("OPTIONS.SPECTATORS_ENABLED"),
    OPTIONS_SPECTATORS_DISABLED("OPTIONS.SPECTATORS_DISABLED"),
    OPTIONS_SHOW_PLAYERS_ENABLED("OPTIONS.SHOWPLAYERS_ENABLED"),
    OPTIONS_SHOW_PLAYERS_DISABLED("OPTIONS.SHOWPLAYERS_DISABLED"),
    SILENT_ENABLED("OPTIONS.SILENT_ENABLED"),
    STAFF_MODE_ENABLE("OPTIONS.STAFF_MODE_ENABLE"),
    STAFF_MODE_DISABLE("OPTIONS.STAFF_MODE_DISABLE"),
    FOLLOW_START("FOLLOW.FOLLOW_START"),
    FOLLOW_END("FOLLOW.FOLLOW_END"),
    FOLLOWED_LEFT("FOLLOW.FOLLOWED_LEFT"),
    SILENT_DISABLED("OPTIONS.SILENT_DISABLED");
    private final String path;

    public String format(Object... objects) {
        return new MessageFormat(ChatColor.translateAlternateColorCodes('&',
                Practice.getInstance().getMessagesConfig().getString(path))).format(objects);
    }

    public String format(Player player, Object... objects) {
        ArrayList<String> list = new ArrayList<>();
        list.add(new MessageFormat(ChatColor.translateAlternateColorCodes('&',
                Practice.getInstance().getMessagesConfig().getString(path))).format(objects));
        return PlaceholderUtil.format(list, player).toString().replace("[", "").replace("]", "");
    }

    public List<String> formatLines(Player player, Object... objects) {
        List<String> lines = new ArrayList<>();

        if (Practice.getInstance().getMessagesConfig().get(path) instanceof String) {
            lines.add(new MessageFormat(ChatColor.translateAlternateColorCodes('&',
                    Practice.getInstance().getMessagesConfig().getString(path))).format(objects));
        } else {
            for (String string : Practice.getInstance().getMessagesConfig().getStringList(path)) {
                lines.add(new MessageFormat(ChatColor.translateAlternateColorCodes('&', string))
                        .format(objects));
            }
        }

        return PlaceholderUtil.format(lines, player);
    }

    public List<String> formatLines(Object... objects) {
        List<String> lines = new ArrayList<>();

        if (Practice.getInstance().getMessagesConfig().get(path) instanceof String) {
            lines.add(new MessageFormat(ChatColor.translateAlternateColorCodes('&',
                    Practice.getInstance().getMessagesConfig().getString(path))).format(objects));
        } else {
            for (String string : Practice.getInstance().getMessagesConfig().getStringList(path)) {
                lines.add(new MessageFormat(ChatColor.translateAlternateColorCodes('&', string))
                        .format(objects));
            }
        }

        return lines;
    }

    @Override
    public String toString() {
        return format();
    }

}
