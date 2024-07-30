package me.lrxh.practice.util;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lrxh.practice.Practice;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.impl.BasicFreeForAllMatch;
import me.lrxh.practice.match.impl.BasicTeamMatch;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.queue.QueueProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class PlaceholderUtil {

    public static List<String> format(List<String> lines, Player player) {
        List<String> formattedLines = new ArrayList<>();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        QueueProfile queueProfile = profile.getQueueProfile();
        for (String line : lines) {
            line = line.replaceAll("<online>", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()));
            line = line.replaceAll("<queued>", String.valueOf(Practice.getInstance().getCache().getPlayers().size()));
            line = line.replaceAll("<in-match>", String.valueOf(Practice.getInstance().getCache().getMatches().size() * 2));
            line = line.replaceAll("<player>", player.getName());
            line = line.replaceAll("<ping>", String.valueOf((BukkitReflection.getPing(player))));
            line = line.replaceAll("<theme>", CC.translate("&" + profile.getOptions().theme().getColor().getChar()));

            if (line.contains("<silent>") && !profile.isSilent()) {
                continue;
            } else {
                line = line.replaceAll("<silent>", "");
            }
            if (line.contains("<follow>") && profile.getFollowing().isEmpty()) {
                continue;
            } else {
                line = line.replaceAll("<follow>", "");
            }

            if (!profile.getFollowing().isEmpty()) {
                line = line.replaceAll("<followedPlayer>", Bukkit.getPlayer(profile.getFollowing().get(0)).getName());
            } else {
                line = line.replaceAll("<followedPlayer>", "");
            }

            if (profile.getState() == ProfileState.QUEUEING) {
                line = line.replaceAll("<kit>", queueProfile.getQueue().getKit().getName());
                line = line.replaceAll("<type>", queueProfile.getQueue().isRanked() ? "Ranked" : "Unranked");
                line = line.replaceAll("<time>", TimeUtil.millisToTimer(queueProfile.getPassed()));
                line = line.replaceAll("<minElo>", String.valueOf(queueProfile.getMinRange()));
                line = line.replaceAll("<maxElo>", String.valueOf(queueProfile.getMaxRange()));
            }

            if (profile.getParty() != null) {
                line = line.replaceAll("<leader>", profile.getParty().getLeader().getName());
                line = line.replaceAll("<party-size>", String.valueOf(profile.getParty().getListOfPlayers().size()));
            }
            Match match = profile.getMatch();
            if (match != null) {
                if (match instanceof BasicTeamMatch) {
                    GameParticipant<MatchGamePlayer> participantA = match.getParticipantA();
                    GameParticipant<MatchGamePlayer> participantB = match.getParticipantB();

                    boolean aTeam = match.getParticipantA().containsPlayer(player.getUniqueId());
                    GameParticipant<MatchGamePlayer> playerTeam = aTeam ? participantA : participantB;
                    GameParticipant<MatchGamePlayer> opponentTeam = aTeam ? participantB : participantA;

                    line = line.replaceAll("<opponentsCount>", String.valueOf(opponentTeam.getAliveCount()))
                            .replaceAll("<opponentsMax>", String.valueOf(opponentTeam.getPlayers().size()))
                            .replaceAll("<teamCount>", String.valueOf(playerTeam.getAliveCount()))
                            .replaceAll("<teamMax>", String.valueOf(playerTeam.getPlayers().size()));
                }
                if (match instanceof BasicFreeForAllMatch) {
                    BasicFreeForAllMatch basicFreeForAllMatch = (BasicFreeForAllMatch) match;
                    line = line.replaceAll("<remaning>", String.valueOf(basicFreeForAllMatch.getRemainingTeams()));
                }

                if (match.getOpponent(player.getUniqueId()) != null) {
                    line = line.replaceAll("<opponent>", match.getOpponent(player.getUniqueId()).getName());
                    line = line.replaceAll("<duration>", match.getDuration());
                    line = line.replaceAll("<opponent-ping>", String.valueOf(BukkitReflection.getPing(match.getOpponent(player.getUniqueId()))));
                    line = line.replaceAll("<your-hits>", String.valueOf(match.getGamePlayer(player).getHits()));
                    line = line.replaceAll("<their-hits>", String.valueOf(match.getGamePlayer(match.getOpponent(player.getUniqueId())).getHits()));
                    line = line.replaceAll("<hitDifference>", getDifference(player, false));
                    line = line.replaceAll("<mmcHitDifference>", getDifference(player, true));
                    line = line.replaceAll("<combo>", getHitCombo(player, false));
                    line = line.replaceAll("<mmcCombo>", getHitCombo(player, true));

                    if (match.getKit().getGameRules().isBedwars()) {
                        line = line.replaceAll("<bedA>", match.isBedABroken() ? CC.RED + CC.X : CC.GREEN + CC.CHECKMARK);
                        line = line.replaceAll("<bedB>", match.isBedBBroken() ? CC.RED + CC.X : CC.GREEN + CC.CHECKMARK);

                        boolean aTeam = match.getParticipantA().containsPlayer(player.getUniqueId());
                        line = line.replaceAll("<youA>", aTeam ? "" : "&7YOU");
                        line = line.replaceAll("<youB>", !aTeam ? "" : "&7YOU");
                    }
                }

                if (profile.getState() == ProfileState.SPECTATING) {
                    line = line.replaceAll("<duration>", match.getDuration());
                }
            }

            if (Practice.getInstance().isPlaceholder()) {
                formattedLines.add(PlaceholderAPI.setPlaceholders(player, line));
            } else {
                formattedLines.add(line);
            }
        }
        return formattedLines;
    }

    public String getDifference(Player player, boolean isMMCDifference) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        Match match = profile.getMatch();
        Int playerHits = match.getGamePlayer(player).getHits();
        Int opponentHits = match.getGamePlayer(match.getOpponent(player.getUniqueId())).getHits()
        String noAdvantage = isMMCDifference ? "&a(+0)" : "&e(0)"

        if (playerHits - opponentHits > 0) {
            return CC.translate("&a(+" + (playerHits - opponentHits) + ")");
        } else if (player - opponent < 0) {
            return CC.translate("&c(" + (playerHits - opponentHits + ")");
        } else {
            return CC.translate(noAdvantage);
        }
    }

    public String getHitCombo(Player player, boolean isMMCCombo) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        Match match = profile.getMatch();
        Int playerCombo = match.getGamePlayer(player).getCombo();
        Int opponentCombo = match.getGamePlayer(match.getOpponent(player.getUniqueId())).getCombo()
        String noCombo = isMMC ? "&f1st to 100 wins!" : "&fNo Combo"

        if (playerCombo > 1) {
            return CC.translate("&a" + playerCombo + " Combo");
        } else if (opponentCombo > 1) {
            return CC.translate("&c" + opponentCombo + " Combo");
        } else if (opponentCombo = 0 && playerCombo = 0) {
            return CC.translate(isMMCCombo);
        }
    }
}
