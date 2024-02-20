package me.lrxh.practice.commands.user.party;

import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = {"party chat", "p chat"})
public class PartyChatCommand {

    public void execute(Player player, String message) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() != null) {
            profile.getParty().sendChat(player, message);
        }
    }

}
