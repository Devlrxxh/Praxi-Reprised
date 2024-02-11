package me.funky.praxi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.funky.praxi.leaderboards.Leaderboard;
import me.funky.praxi.leaderboards.PlayerElo;
import me.funky.praxi.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "praxi";
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
        return Bukkit.getPluginManager().isPluginEnabled("Praxi");
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) return "";
        if (!player.isOnline()) return "Offline Player";
        String[] parts = identifier.split("_");
        if (parts.length == 4 && parts[0].equalsIgnoreCase("lb")) {
            String queue = parts[1];
            int position = Integer.parseInt(parts[2]);
            PlayerElo playerElo = Leaderboard.getEloLeaderboards().get(queue).getTopPlayers().get(position - 1);

            switch (parts[3]) {
                case "name":
                    return playerElo.getPlayerName();
                case "elo":
                    return String.valueOf(playerElo.getElo());
            }
        } else if (identifier.equalsIgnoreCase("leaderboards_update")) {
            return TimeUtil.millisToTimer(Leaderboard.getRefreshTime());
        }
        return null;
    }
}
