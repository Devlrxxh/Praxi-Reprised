package me.lrxh.practice.commands.user.party;

import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.party.Party;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = {"p create", "party create"})
public class PartyCreateCommand {

    public void execute(Player player) {
        if (player.hasMetadata("frozen")) {
            player.sendMessage(CC.RED + "You cannot create a party while frozen.");
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() != null) {
            player.sendMessage(CC.RED + "You already have a party.");
            return;
        }

        if (profile.getState() != ProfileState.LOBBY) {
            player.sendMessage(CC.RED + "You must be in the lobby to create a party.");
            return;
        }

        profile.setParty(new Party(player));

        Practice.getInstance().getHotbar().giveHotbarItems(player);

        player.sendMessage(Locale.PARTY_CREATE.format(player));
    }

}
