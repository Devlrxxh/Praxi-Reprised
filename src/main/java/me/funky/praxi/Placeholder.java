package me.funky.praxi;


import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.funky.praxi.leaderboards.Leaderboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        switch (identifier) {
            case "top_1":
                return Leaderboard.getTopPositions().get(0).getPlayerElo().getPlayerName();
            case "top_1_elo":
                return String.valueOf(Leaderboard.getTopPositions().get(0).getPlayerElo().getElo());
            case "top_2":
                return Leaderboard.getTopPositions().get(1).getPlayerElo().getPlayerName();
            case "top_2_elo":
                return String.valueOf(Leaderboard.getTopPositions().get(1).getPlayerElo().getElo());
            case "top_3":
                return Leaderboard.getTopPositions().get(2).getPlayerElo().getPlayerName();
            case "top_3_elo":
                return String.valueOf(Leaderboard.getTopPositions().get(2).getPlayerElo().getElo());
        }
        return null;
    }

}