package me.funky.praxi.commands.user.party;

import me.funky.praxi.Locale;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = { "p", "p help", "party", "party help" })
public class PartyHelpCommand {

	public void execute(Player player) {
		Locale.PARTY_HELP.formatLines().forEach(player::sendMessage);
	}

}
