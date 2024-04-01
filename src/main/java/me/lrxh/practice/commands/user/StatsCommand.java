package me.lrxh.practice.commands.user;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.menus.StatsMenu;
import me.lrxh.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("stats")
@Description("Display player stats.")
public class StatsCommand extends BaseCommand {

    @Default
    public void open(Player player) {
        new StatsMenu(player.getName()).openMenu(player);

    }

    @Default
    @Syntax("<name>")
    @CommandCompletion("@names")
    public void statsOthers(Player player, String otherPlayer) {
        if (Bukkit.getPlayer(otherPlayer) == null) {
            player.sendMessage(CC.translate("&4ERROR - &cPlayer isn't online!"));
            return;
        }
        new StatsMenu(otherPlayer).openMenu(player);
    }
}
