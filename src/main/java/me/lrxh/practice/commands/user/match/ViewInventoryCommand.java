package me.lrxh.practice.commands.user.match;

import me.lrxh.practice.match.MatchSnapshot;
import me.lrxh.practice.match.menu.MatchDetailsMenu;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

import java.util.UUID;

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
