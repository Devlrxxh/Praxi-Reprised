package me.funky.praxi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.funky.praxi.leaderboards.Leaderboard;
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

        if (identifier.startsWith("top_")) {
            String[] parts = identifier.split("_");
            if (parts.length == 2 || (parts.length == 3 && (parts[2].equalsIgnoreCase("elo") || parts[2].equalsIgnoreCase("kills") || parts[2].equalsIgnoreCase("loses")))) {
                int topNumber;
                try {
                    topNumber = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    return "Invalid placeholder";
                }

                if (topNumber <= Leaderboard.getEloPositions().size()) {
                    if (parts.length == 3) {
                        if (parts[2].equalsIgnoreCase("elo")) {
                            return String.valueOf(Leaderboard.getEloPositions().get(topNumber - 1).getPlayerElo().getElo());
                        }
                    } else {
                        return Leaderboard.getEloPositions().get(topNumber - 1).getPlayerElo().getPlayerName();
                    }
                }
            }
        }

        return null;
    }
}
