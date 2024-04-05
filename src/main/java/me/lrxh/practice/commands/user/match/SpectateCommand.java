package me.lrxh.practice.commands.user.match;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("spectate|spec")
@Description("Spectate a player.")
public class SpectateCommand extends BaseCommand {
    @Default
    @Syntax("<name>")
    @CommandCompletion("@names")
    public void execute(Player player, String targetName) {
        if (player.hasMetadata("frozen")) {
            player.sendMessage(CC.RED + "You cannot spectate while frozen.");
            return;
        }
        if (Bukkit.getPlayer(targetName) == null) {
            player.sendMessage(CC.RED + "A player with that name could not be found.");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(CC.RED + "A player with that name could not be found.");
            return;
        }

        Profile playerProfile = Profile.getByUuid(player.getUniqueId());

        if (playerProfile.isBusy()) {
            player.sendMessage(CC.RED + "You must be in the lobby and not queueing to spectate.");
            return;
        }

        if (playerProfile.getParty() != null) {
            player.sendMessage(CC.RED + "You must leave your party to spectate a match.");
            return;
        }

        Profile targetProfile = Profile.getByUuid(target.getUniqueId());

        if (targetProfile.getState() != ProfileState.FIGHTING) {
            player.sendMessage(CC.RED + "That player is not in a match.");
            return;
        }

        if (!targetProfile.getOptions().allowSpectators()) {
            player.sendMessage(CC.RED + "That player is not allowing spectators.");
            return;
        }

        targetProfile.getMatch().addSpectator(player, target);
    }

    @Default
    @Subcommand("leave")
    public void leave(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getMatch() != null && profile.getMatch().getGamePlayer(player).isDead()) {
            profile.getMatch().getGamePlayer(player).setDisconnected(true);
            profile.setState(ProfileState.LOBBY);
            profile.setMatch(null);
        } else if (profile.getState() == ProfileState.SPECTATING) {
            profile.getMatch().removeSpectator(player);
        } else {
            player.sendMessage(CC.RED + "You are not spectating a match.");
        }
    }
}
