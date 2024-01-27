package me.funky.praxi.commands.event.user;

import me.funky.praxi.event.game.EventGame;
import me.funky.praxi.util.Cooldown;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandMeta(label = {"event clearcooldown", "event clearcd"}, permission = "praxi.admin.event")
public class EventClearCooldownCommand {

    public void execute(CommandSender sender) {
        EventGame.setCooldown(new Cooldown(0));
        sender.sendMessage(ChatColor.GREEN + "You cleared the event cooldown.");
    }

}
