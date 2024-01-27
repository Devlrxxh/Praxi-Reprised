package me.funky.praxi.commands.admin.general;

import me.funky.praxi.util.command.command.CommandMeta;
import me.funky.praxi.Praxi;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;

@CommandMeta(label = { "setspawn" }, permission = "praxi.setspawn")
public class SetSpawnCommand
{
    public void execute(Player player) {
        Praxi.getInstance().getEssentials().setSpawn(player.getLocation());
        player.sendMessage(CC.translate("&bSpawn set successfully!"));
    }
}
