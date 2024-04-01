package me.lrxh.practice.commands.user;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("elo")
@Description("Displays player elo.")
public class EloCommand extends BaseCommand {

    @Default
    public void elo(Player player) {
        Profile profile = Profile.getProfiles().get(player.getUniqueId());
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&f&m---------------------"));
        player.sendMessage(CC.translate("&eYour elo!"));
        for (Queue queue : Practice.getInstance().getCache().getQueues()) {
            if (queue.isRanked()) {
                player.sendMessage(CC.translate("&c• &e" + queue.getKit().getName() + "&7: &f" + profile.getKitData().get(queue.getKit()).getElo()));
            }
        }
        player.sendMessage(CC.translate("&f&m---------------------"));
        player.sendMessage(" ");
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
        Profile profile = Profile.getProfiles().get(otherP.getUniqueId());
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&f&m---------------------"));
        player.sendMessage(CC.translate("&e" + otherPlayer + "'s elo!"));
        for (Queue queue : Practice.getInstance().getCache().getQueues()) {
            if (queue.isRanked()) {
                player.sendMessage(CC.translate("&c• &e" + queue.getKit().getName() + "&7: &f" + profile.getKitData().get(queue.getKit()).getElo()));
            }
        }
        player.sendMessage(CC.translate("&f&m---------------------"));
        player.sendMessage(" ");
    }
}
