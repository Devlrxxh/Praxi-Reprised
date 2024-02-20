package me.lrxh.practice.commands.user;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import me.lrxh.practice.menus.StatsMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("stats")
@Description("Display player stats.")
public class StatsCommand extends BaseCommand {

    @Default
    public void open(Player player) {
        new StatsMenu().openMenu(player);

    }

    @CommandCompletion("@names")
    public void statsOthers(Player player, String otherPlayer) {
        new StatsMenu().openMenu(Bukkit.getPlayer(otherPlayer));
    }
}
