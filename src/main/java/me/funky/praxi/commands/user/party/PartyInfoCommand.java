package me.funky.praxi.commands.user.party;

import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = {"p info", "party info"})
public class PartyInfoCommand {

    public void execute(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        profile.getParty().sendInformation(player);
    }

}
