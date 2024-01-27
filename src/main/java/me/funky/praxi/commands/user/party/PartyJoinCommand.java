package me.funky.praxi.commands.user.party;

import me.funky.praxi.party.Party;
import me.funky.praxi.party.PartyPrivacy;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandMeta(label = { "p join", "party join" })
public class PartyJoinCommand {

	public void execute(Player player, Player target) {
		if (target == null) {
			player.sendMessage(CC.RED + "A player with that name could not be found.");
			return;
		}

		if (player.hasMetadata("frozen")) {
			player.sendMessage(CC.RED + "You cannot join a party while frozen.");
			return;
		}

		Profile profile = Profile.getByUuid(player.getUniqueId());

		if (profile.getParty() != null) {
			player.sendMessage(CC.RED + "You already have a party.");
			return;
		}

		Profile targetProfile = Profile.getByUuid(target.getUniqueId());
		Party party = targetProfile.getParty();

		if (party == null) {
			player.sendMessage(CC.RED + "A party with that name could not be found.");
			return;
		}

		if (party.getPrivacy() == PartyPrivacy.CLOSED) {
			if (party.getInvite(player.getUniqueId()) == null) {
				player.sendMessage(CC.RED + "You have not been invited to that party.");
				return;
			}
		}

		if (party.getPlayers().size() >= 32) {
			player.sendMessage(CC.RED + "That party is full and cannot hold anymore players.");
			return;
		}

		party.join(player);
	}

}
