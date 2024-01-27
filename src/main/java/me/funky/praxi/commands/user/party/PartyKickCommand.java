package me.funky.praxi.commands.user.party;

import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandMeta(label = { "p kick", "party kick" })
public class PartyKickCommand {

	public void execute(Player player, Player target) {
		Profile profile = Profile.getByUuid(player.getUniqueId());

		if (profile.getParty() == null) {
			player.sendMessage(CC.RED + "You do not have a party.");
			return;
		}

		if (!profile.getParty().getLeader().equals(player)) {
			player.sendMessage(CC.RED + "You are not the leader of your party.");
			return;
		}

		if (!profile.getParty().containsPlayer(target.getUniqueId())) {
			player.sendMessage(CC.RED + "That player is not a member of your party.");
			return;
		}

		if (player.equals(target)) {
			player.sendMessage(CC.RED + "You cannot kick yourself from your party.");
			return;
		}

		profile.getParty().leave(target, true);
	}

}
