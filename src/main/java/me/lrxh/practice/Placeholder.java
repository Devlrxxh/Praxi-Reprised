package me.lrxh.practice;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lrxh.practice.leaderboards.Leaderboard;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "practice";
    }

    @Override
    public @NotNull String getAuthor() {
        return "lrxh";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return Bukkit.getPluginManager().isPluginEnabled(Practice.getInstance());
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) return "";
        if (!player.isOnline()) return "Offline Player";

        String[] parts = identifier.split("_");
        if (parts.length >= 4 && parts.length <= 6 && parts[0].equalsIgnoreCase("lb")) {
            String queue = parts[1];
            int position = Integer.parseInt(parts[2]);
            String valueType = parts[parts.length - 1];
            boolean showName = parts[parts.length - 2].equalsIgnoreCase("name");

            if (position > 10 || position < 1) return "";

            switch (valueType) {
                case "elo":
                    return showName ?
                            Leaderboard.getEloLeaderboards().get(queue).getTopEloPlayers().get(position - 1).getPlayerName() :
                            String.valueOf(Leaderboard.getEloLeaderboards().get(queue).getTopEloPlayers().get(position - 1).getElo());
                case "kills":
                    return showName ?
                            Leaderboard.getEloLeaderboards().get(queue).getTopKillPlayers().get(position - 1).getPlayerName() :
                            String.valueOf(Leaderboard.getEloLeaderboards().get(queue).getTopKillPlayers().get(position - 1).getKills());
            }
        } else if (identifier.equalsIgnoreCase("leaderboards_update")) {
            return TimeUtil.millisToTimer(Leaderboard.getRefreshTime());
        } else if (identifier.equalsIgnoreCase("player_theme")) {
            return String.valueOf(Profile.getByUuid(player.getUniqueId()).getOptions().theme().getColor().getChar());
        }
        return "test";
    }
}
