package me.funky.praxi.commands.admin.general;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.funky.praxi.Praxi;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;


@CommandAlias("praxi")
@CommandPermission("praxi.admin.main")
@Description("Main Command for Praxi Practice Core.")
public class MainCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    public void help(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&c" + Praxi.getInstance().getName() + " Practice Core"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/praxi setspawn &7- &fSet server spawn"));
        player.sendMessage(CC.translate("&7* &c/praxi reload &7- &fReload all configs"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("setspawn")
    public void setspawn(Player player) {
        Praxi.getInstance().getEssentials().setSpawn(player.getLocation());
        player.sendMessage(CC.translate("&aSuccessfully set spawn!"));
    }

    @Subcommand("reload")
    public void reload(Player player) {
        Praxi.getInstance().loadConfigs();
        player.sendMessage(CC.translate("&aSuccessfully reloaded configs!"));
    }
}
