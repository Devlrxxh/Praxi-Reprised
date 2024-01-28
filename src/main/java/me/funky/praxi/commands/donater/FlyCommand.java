package me.funky.praxi.commands.donater;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandAlias("fly")
@CommandPermission("praxi.donor.fly")
@Description("Fly Command.")
public class FlyCommand extends BaseCommand {

    @Default
    public void execute(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.QUEUEING) {
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.updateInventory();
                player.sendMessage(CC.YELLOW + "You are no longer flying.");
            } else {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.updateInventory();
                player.sendMessage(CC.YELLOW + "You are now flying.");
            }
        } else {
            player.sendMessage(CC.RED + "You cannot fly right now.");
        }
    }

}
