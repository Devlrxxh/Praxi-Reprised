package me.funky.praxi.commands.user.duels;

import me.funky.praxi.duel.DuelProcedure;
import me.funky.praxi.duel.DuelRequest;
import me.funky.praxi.duel.menu.DuelSelectKitMenu;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CPL;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = "duel")
public class DuelCommand {

    public void execute(Player sender, @CPL("player") Player target) {
        if (target == null) {
            sender.sendMessage(CC.RED + "A player with that name could not be found.");
            return;
        }

        if (sender.hasMetadata("frozen")) {
            sender.sendMessage(CC.RED + "You cannot duel while frozen.");
            return;
        }

        if (target.hasMetadata("frozen")) {
            sender.sendMessage(CC.RED + "You cannot duel a frozen player.");
            return;
        }

        //if (sender.getUniqueId().equals(target.getUniqueId())) {
        //    sender.sendMessage(CC.RED + "You cannot duel yourself.");
        //    return;
        //}

        Profile senderProfile = Profile.getByUuid(sender.getUniqueId());
        Profile targetProfile = Profile.getByUuid(target.getUniqueId());

        if (senderProfile.isBusy()) {
            sender.sendMessage(CC.RED + "You cannot duel right now.");
            return;
        }

        if (targetProfile.isBusy()) {
            sender.sendMessage(target.getDisplayName() + CC.RED + " is currently busy.");
            return;
        }

        if (!targetProfile.getOptions().receiveDuelRequests()) {
            sender.sendMessage(CC.RED + "That player is not accepting duel requests at the moment.");
            return;
        }

        DuelRequest duelRequest = targetProfile.getDuelRequest(sender);

        if (duelRequest != null) {
            if (!senderProfile.isDuelRequestExpired(duelRequest)) {
                sender.sendMessage(CC.RED + "You already sent that player a duel request.");
                return;
            }
        }

        if (senderProfile.getParty() != null && targetProfile.getParty() == null) {
            sender.sendMessage(CC.RED + "You cannot send a party duel request to a player that is not in a party.");
            return;
        }

        if (senderProfile.getParty() == null && targetProfile.getParty() != null) {
            sender.sendMessage(CC.RED + "You cannot send a duel request to a player in a party.");
            return;
        }

        if (senderProfile.getParty() != null && targetProfile.getParty() != null) {
            if (senderProfile.getParty().equals(targetProfile.getParty())) {
                sender.sendMessage(CC.RED + "You cannot duel your own party.");
                return;
            }
        }

        DuelProcedure procedure = new DuelProcedure(sender, target, senderProfile.getParty() != null);
        senderProfile.setDuelProcedure(procedure);

        new DuelSelectKitMenu().openMenu(sender);
    }

}
