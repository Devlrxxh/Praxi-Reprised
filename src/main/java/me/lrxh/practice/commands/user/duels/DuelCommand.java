package me.lrxh.practice.commands.user.duels;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.Practice;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.duel.DuelProcedure;
import me.lrxh.practice.duel.DuelRequest;
import me.lrxh.practice.duel.menu.DuelSelectKitMenu;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.impl.BasicTeamMatch;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.participant.TeamGameParticipant;
import me.lrxh.practice.party.Party;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("duel")
@Description("Duel Command.")
public class DuelCommand extends BaseCommand {

    @Default
    @Syntax("<name>")
    @CommandCompletion("@names")
    public void execute(Player sender, String targetName) {
        if (Bukkit.getPlayer(targetName) == null) {
            sender.sendMessage(CC.RED + "A player with that name could not be found.");
            return;
        }
        Player target = Bukkit.getPlayer(targetName);

        if (sender.hasMetadata("frozen")) {
            sender.sendMessage(CC.RED + "You cannot duel while frozen.");
            return;
        }

        if (target.hasMetadata("frozen")) {
            sender.sendMessage(CC.RED + "You cannot duel a frozen player.");
            return;
        }

        if (Practice.getInstance().isReplay()) {
            if (PlayerUtil.inReplay(target)) {
                sender.sendMessage(CC.RED + "You cannot duel a replaying player.");
                return;
            }
        }

        Profile targetProfile = Profile.getByUuid(target.getUniqueId());
//        if (sender.getUniqueId().equals(target.getUniqueId())) {
//            sender.sendMessage(CC.RED + "You cannot duel yourself.");
//            return;
//        }

        Profile senderProfile = Profile.getByUuid(sender.getUniqueId());

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

        if (senderProfile.getParty() != null) {
            if (senderProfile.getParty().equals(targetProfile.getParty())) {
                sender.sendMessage(CC.RED + "You cannot duel your own party.");
                return;
            }
        }

        DuelProcedure procedure = new DuelProcedure(sender, target, senderProfile.getParty() != null);
        senderProfile.setDuelProcedure(procedure);

        new DuelSelectKitMenu().openMenu(sender);
    }

    @Subcommand("accept")
    public void accept(Player player, String targetName) {
        if (Bukkit.getPlayer(targetName) == null) {
            player.sendMessage(CC.RED + "That player is no longer online.");
            return;
        }
        Player target = Bukkit.getPlayer(targetName);

        if (player.hasMetadata("frozen")) {
            player.sendMessage(CC.RED + "You cannot duel while frozen.");
            return;
        }

        if (target.hasMetadata("frozen")) {
            player.sendMessage(CC.RED + "You cannot duel a frozen player.");
            return;
        }

        Profile playerProfile = Profile.getByUuid(player.getUniqueId());

        if (playerProfile.isBusy()) {
            player.sendMessage(CC.RED + "You cannot duel right now.");
            return;
        }

        Profile targetProfile = Profile.getByUuid(target.getUniqueId());

        if (targetProfile.isBusy()) {
            player.sendMessage(target.getDisplayName() + CC.RED + " is currently busy.");
            return;
        }

        DuelRequest duelRequest = playerProfile.getDuelRequest(target);

        if (duelRequest != null) {
            if (targetProfile.isDuelRequestExpired(duelRequest)) {
                player.sendMessage(CC.RED + "That duel request has expired!");
                return;
            }

            if (duelRequest.isParty()) {
                if (playerProfile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party to duel with.");
                    return;
                } else if (targetProfile.getParty() == null) {
                    player.sendMessage(CC.RED + "That player does not have a party to duel with.");
                    return;
                }
            } else {
                if (playerProfile.getParty() != null) {
                    player.sendMessage(CC.RED + "You cannot duel whilst in a party.");
                    return;
                } else if (targetProfile.getParty() != null) {
                    player.sendMessage(CC.RED + "That player is in a party and cannot duel right now.");
                    return;
                }
            }

            Arena arena = duelRequest.getArena();

            if (arena.isActive()) {
                arena = Arena.getRandomArena(duelRequest.getKit());
            }

            if (arena == null) {
                player.sendMessage(CC.RED + "Tried to start a match but there are no available arenas.");
                return;
            }

            playerProfile.getDuelRequests().remove(duelRequest);

            arena.setActive(true);

            GameParticipant<MatchGamePlayer> participantA = null;
            GameParticipant<MatchGamePlayer> participantB = null;

            if (duelRequest.isParty()) {
                for (Party party : new Party[]{playerProfile.getParty(), targetProfile.getParty()}) {
                    Player leader = party.getLeader();
                    MatchGamePlayer gamePlayer = new MatchGamePlayer(leader.getUniqueId(), leader.getName());
                    TeamGameParticipant<MatchGamePlayer> participant = new TeamGameParticipant<>(gamePlayer);

                    for (Player partyPlayer : party.getListOfPlayers()) {
                        if (!partyPlayer.getPlayer().equals(leader)) {
                            participant.getPlayers().add(new MatchGamePlayer(partyPlayer.getUniqueId(),
                                    partyPlayer.getName()));
                        }
                    }

                    if (participantA == null) {
                        participantA = participant;
                    } else {
                        participantB = participant;
                    }
                }
            } else {
                MatchGamePlayer playerA = new MatchGamePlayer(player.getUniqueId(), player.getName());
                MatchGamePlayer playerB = new MatchGamePlayer(target.getUniqueId(), target.getName());

                participantA = new GameParticipant<>(playerA);
                participantB = new GameParticipant<>(playerB);
            }

            Match match = new BasicTeamMatch(null, duelRequest.getKit(), arena, false, participantA, participantB, true);
            match.start();
        } else {
            player.sendMessage(CC.RED + "You do not have a duel request from that player.");
        }
    }

}
