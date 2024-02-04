package me.funky.praxi.commands.event.user;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import me.funky.praxi.event.game.menu.EventHostMenu;
import org.bukkit.entity.Player;

@CommandAlias("hosts")
@Description("Host Events.")
public class HostCommand extends BaseCommand {
    @Default
    public void open(Player player) {
        new EventHostMenu().openMenu(player);
    }
}
