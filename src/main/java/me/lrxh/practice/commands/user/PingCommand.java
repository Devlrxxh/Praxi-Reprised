package me.lrxh.practice.commands.user;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.Locale;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.BukkitReflection;
import me.lrxh.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("ping")
@Description("Display player ping.")
public class PingCommand extends BaseCommand {

    @Default
    public void ping(Player player) {
        Profile profile = Profile.getProfiles().get(player.getUniqueId());

        if (profile.getMatch() != null) {
            Match match = profile.getMatch();
            player.sendMessage(Locale.PING_YOUR.format(player, BukkitReflection.getPing(player)));
            player.sendMessage(Locale.PING_OTHERS.format(player, BukkitReflection.getPing(match.getOpponent(player.getUniqueId())), match.getOpponent(player.getUniqueId()).getName()));
        } else {
            player.sendMessage(Locale.PING_YOUR.format(player, BukkitReflection.getPing(player)));
        }
    }

    @Default
    @Syntax("<name>")
    @CommandCompletion("@names")
    public void pingOthers(Player player, String otherPlayer) {
        if (Bukkit.getPlayer(otherPlayer) == null) {
            player.sendMessage(CC.translate("&4ERROR - &cPlayer isn't online!"));
            return;
        }
        Player otherP = Bukkit.getPlayer(otherPlayer);
        player.sendMessage(Locale.PING_OTHERS.format(player, BukkitReflection.getPing(otherP), otherP.getName()));

    }
}
