package me.lrxh.practice.commands.admin.general;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.Locale;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("follow")
@Description("Follow a player.")
@CommandPermission("practice.admin.follow")
public class FollowCommand extends BaseCommand {


    @Default
    @Syntax("<name>")
    @CommandCompletion("@names")
    public void pingOthers(Player player, String otherPlayer) {
        if (Bukkit.getPlayer(otherPlayer) == null) {
            player.sendMessage(CC.translate("&4ERROR - &cPlayer isn't online!"));
            return;
        }
        if (Bukkit.getPlayer(otherPlayer).equals(player)) {
            player.sendMessage(CC.translate("&4ERROR - &cYou cannot follow yourself!"));
            return;
        }
        Player otherP = Bukkit.getPlayer(otherPlayer);

        Profile playerProfile = Profile.getProfiles().get(player.getUniqueId());
        if (!playerProfile.getFollowing().isEmpty() && !playerProfile.getFollowing().contains(otherP.getUniqueId())) {
            player.sendMessage(CC.translate("&4ERROR - &cYou cannot follow multiple players!"));
            return;
        }

        Profile profile = Profile.getProfiles().get(otherP.getUniqueId());

        if (profile.getFollowers().contains(player.getUniqueId())) {
            profile.getFollowers().remove(player.getUniqueId());
            playerProfile.getFollowing().remove(otherP.getUniqueId());
            player.sendMessage(Locale.FOLLOW_END.format(player, otherP.getName()));
        } else {
            profile.getFollowers().add(player.getUniqueId());
            playerProfile.getFollowing().add(otherP.getUniqueId());
            player.sendMessage(Locale.FOLLOW_START.format(player, otherP.getName()));
        }
    }
}
