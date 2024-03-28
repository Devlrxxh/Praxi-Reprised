package me.lrxh.practice.commands.user.duels;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.meta.ProfileRematchData;
import me.lrxh.practice.util.CC;
import org.bukkit.entity.Player;

@CommandAlias("rematch")
@Description("Rematch Command.")
public class RematchCommand extends BaseCommand {

    @Default
    @Syntax("<name>")
    @CommandCompletion("@names")
    public void execute(Player player, String targetName) {
        if (player.hasMetadata("frozen")) {
            player.sendMessage(CC.RED + "You cannot duel while frozen.");
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        ProfileRematchData rematchData = profile.getRematchData();

        if (rematchData == null) {
            player.sendMessage(CC.RED + "You do not have anyone to rematch.");
            return;
        }

        rematchData.validate();

        if (rematchData.isCancelled()) {
            player.sendMessage(CC.RED + "You can no longer send that player a rematch.");
            return;
        }

        if (rematchData.isReceive()) {
            rematchData.accept();
        } else {
            if (rematchData.isSent()) {
                player.sendMessage(CC.RED + "You have already sent a rematch to that player.");
                return;
            }

            rematchData.request();
        }
    }
}
