package me.funky.praxi.commands.user.gamer;

import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = "suicide")
public class SuicideCommand {
    public void execute(Player player) {
        player.setHealth(0);
        player.sendMessage(CC.translate("&cYou have killed yourself! Oh noes"));
    }
}
