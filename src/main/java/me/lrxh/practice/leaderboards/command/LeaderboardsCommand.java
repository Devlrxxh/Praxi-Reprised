package me.lrxh.practice.leaderboards.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.leaderboards.Leaderboard;
import me.lrxh.practice.leaderboards.LeaderboardsMenu;
import me.lrxh.practice.util.CC;
import org.bukkit.entity.Player;

@CommandAlias("leaderboard|leaderboards|lb|lbs")
@Description("Open leaderboards.")
public class LeaderboardsCommand extends BaseCommand {

    @Default
    public void open(Player player) {
        new LeaderboardsMenu(false).openMenu(player);
    }

    @Subcommand("refresh")
    @CommandPermission("practice.leaderboards.refresh")
    public void refresh(Player player) {
        Leaderboard.getEloLeaderboards().clear();
        Leaderboard.setEloLeaderboards(Leaderboard.init());
        player.sendMessage(CC.translate("&aRefreshed Leaderboards!"));
    }

    @Subcommand("kills")
    public void kills(Player player) {
        new LeaderboardsMenu(true).openMenu(player);
    }

    @Subcommand("elo")
    public void elo(Player player) {
        new LeaderboardsMenu(false).openMenu(player);
    }
}
