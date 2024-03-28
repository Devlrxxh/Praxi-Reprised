package me.lrxh.practice.commands.user.match;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import me.lrxh.practice.match.MatchSnapshot;
import me.lrxh.practice.match.menu.MatchDetailsMenu;
import me.lrxh.practice.util.CC;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("viewinv")
@Description("View post match inventories.")
public class ViewInventoryCommand extends BaseCommand {

    @Default
    @Syntax("<uuid>")
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
