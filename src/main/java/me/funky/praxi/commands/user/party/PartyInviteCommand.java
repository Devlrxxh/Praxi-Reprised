package me.funky.praxi.commands.user.party;

import me.funky.praxi.party.PartyPrivacy;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandMeta(label = { "p invite", "party invite" })
public class PartyInviteCommand {

	public void execute(Player player, Player target) {
		if (target == null) {
			player.sendMessage(CC.RED + "A player with that name could not be found.");
			return;
		}

		Profile profile = Profile.getByUuid(player.getUniqueId());

		if (profile.getParty() == null) {
			player.sendMessage(CC.RED + "You do not have a party.");
			return;
		}

		if (!profile.getParty().getLeader().equals(player)) {
			player.sendMessage(CC.RED + "You are not the leader of your party.");
			return;
		}

		if (profile.getParty().getInvite(target.getUniqueId()) != null) {
			player.sendMessage(CC.RED + "That player has already been invited to your party.");
			return;
		}

		if (profile.getParty().containsPlayer(target.getUniqueId())) {
			player.sendMessage(CC.RED + "That player is already in your party.");
			return;
		}

		if (profile.getParty().getPrivacy() == PartyPrivacy.OPEN) {
			player.sendMessage(CC.RED + "The party state is Open. You do not need to invite players.");
			return;
		}

		Profile targetData = Profile.getByUuid(target.getUniqueId());

		if (targetData.isBusy()) {
			player.sendMessage(target.getDisplayName() + CC.RED + " is currently busy.");
			return;
		}

		profile.getParty().invite(target);
	}

}
