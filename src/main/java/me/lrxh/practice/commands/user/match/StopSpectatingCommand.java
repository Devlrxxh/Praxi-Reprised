package me.lrxh.practice.commands.user.match;

import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = "spec leave")
public class StopSpectatingCommand {

    public void execute(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getState() == ProfileState.FIGHTING && profile.getMatch().getGamePlayer(player).isDead()) {
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
