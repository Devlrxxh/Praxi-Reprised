package me.lrxh.practice.commands.user;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.Locale;
import me.lrxh.practice.util.BukkitReflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("ping")
@Description("Display player ping.")
public class PingCommand extends BaseCommand {

    @Default
    public void ping(Player player) {
        player.sendMessage(Locale.PING_YOUR.format(player, BukkitReflection.getPing(player)));
    }

    @Default
    @Syntax("<name>")
    @CommandCompletion("@names")
    public void pingOthers(Player player, String otherPlayer) {
        player.sendMessage(Locale.PING_OTHERS.format(player, BukkitReflection.getPing(Bukkit.getPlayer(otherPlayer)), Bukkit.getPlayer(otherPlayer).getName()));
    }
}
