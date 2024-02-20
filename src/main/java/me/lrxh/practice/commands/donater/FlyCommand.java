package me.lrxh.practice.commands.donater;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.util.CC;
import org.bukkit.entity.Player;

@CommandAlias("fly")
@CommandPermission("practice.donor.fly")
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
