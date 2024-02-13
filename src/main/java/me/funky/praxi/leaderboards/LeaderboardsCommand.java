package me.funky.praxi.leaderboards;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandAlias("leaderboard|leaderboards|lb|lbs")
@Description("Open leaderboards.")
public class LeaderboardsCommand extends BaseCommand {

    @Default
    public void open(Player player) {
        new LeaderboardsMenu().openMenu(player);
    }

    @Subcommand("refresh")
    @CommandPermission("praxi.leaderboards.refresh")
    public void refresh(Player player) {
        Leaderboard.getEloLeaderboards().clear();
        Leaderboard.setEloLeaderboards(Leaderboard.init());
        player.sendMessage(CC.translate("&aRefreshed Leaderboards!"));
    }
}
