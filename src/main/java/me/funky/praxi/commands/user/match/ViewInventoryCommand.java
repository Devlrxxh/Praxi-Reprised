package me.funky.praxi.commands.user.match;

import me.funky.praxi.match.MatchSnapshot;
import me.funky.praxi.match.menu.MatchDetailsMenu;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import java.util.UUID;

import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandMeta(label = "viewinv")
public class ViewInventoryCommand {

	public void execute(Player player, String id) {
		MatchSnapshot cachedInventory;

		try {
			cachedInventory = MatchSnapshot.getByUuid(UUID.fromString(id));
		} catch (Exception e) {
			cachedInventory = MatchSnapshot.getByName(id);
		}

		if (cachedInventory == null) {
			player.sendMessage(CC.RED + "Couldn't find an inventory for that ID.");
			return;
		}

		new MatchDetailsMenu(cachedInventory).openMenu(player);
	}

}
