package me.lrxh.practice.commands.user.party;

import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = {"p leave", "party leave"})
public class PartyLeaveCommand {

    public void execute(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (profile.getParty().getLeader().equals(player)) {
            profile.getParty().disband();
        } else {
            profile.getParty().leave(player, false);
        }
    }

}
