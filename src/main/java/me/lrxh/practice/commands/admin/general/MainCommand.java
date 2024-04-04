package me.lrxh.practice.commands.admin.general;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.Practice;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;


@CommandAlias("practice")
@CommandPermission("practice.admin.main")
@Description("Main Command for Practice Practice Core.")
public class MainCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    public void help(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&c" + Practice.getInstance().getName() + " Core"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/practice setspawn &7- &fSet server spawn"));
        player.sendMessage(CC.translate("&7* &c/practice reload &7- &fReload all configs"));
        player.sendMessage(CC.translate("&7* &c/practice clear &7- &fClear all items"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("setspawn")
    public void setspawn(Player player) {
        setSpawn(player.getLocation());
        player.sendMessage(CC.translate("&aSuccessfully set spawn!"));
    }

    public void setSpawn(Location location) {
        Practice.getInstance().getCache().setSpawn(location);

        if (Practice.getInstance().getCache().getSpawn() == null) {
            Practice.getInstance().getMainConfig().getConfiguration().set("ESSENTIAL.SPAWN_LOCATION", null);
        } else {
            Practice.getInstance().getMainConfig().getConfiguration().set("ESSENTIAL.SPAWN_LOCATION", LocationUtil.serialize(Practice.getInstance().getCache().getSpawn()));
        }

        try {
            Practice.getInstance().getMainConfig().getConfiguration().save(Practice.getInstance().getMainConfig().getFile());
        } catch (IOException ignored) {
        }
    }


    @Subcommand("reload")
    public void reload(Player player) {
        Practice.getInstance().loadConfigs();
        player.sendMessage(CC.translate("&aSuccessfully reloaded configs!"));
    }

    @Subcommand("clear")
    public void clear(Player player) {
        Practice.getInstance().clearEntities();
    }
}
