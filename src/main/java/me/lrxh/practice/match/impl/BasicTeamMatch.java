package me.lrxh.practice.match.impl;

import lombok.Getter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.MatchSnapshot;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.meta.ProfileRematchData;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ChatComponentBuilder;
import me.lrxh.practice.util.ChatHelper;
import me.lrxh.practice.util.PlayerUtil;
import me.lrxh.practice.util.elo.EloUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
public class BasicTeamMatch extends Match {

    private final GameParticipant<MatchGamePlayer> participantA;
    private final GameParticipant<MatchGamePlayer> participantB;
    private GameParticipant<MatchGamePlayer> winningParticipant;
    private GameParticipant<MatchGamePlayer> losingParticipant;

    public BasicTeamMatch(Queue queue, Kit kit, Arena arena, boolean ranked, GameParticipant<MatchGamePlayer> participantA,
                          GameParticipant<MatchGamePlayer> participantB, boolean duel) {
        super(queue, kit, arena, ranked, duel);

        this.participantA = participantA;
        this.participantB = participantB;
        Practice.getInstance().getCache().getMatch(getMatchId()).setDuel(duel);
    }

    @Override
    public void setupPlayer(Player player) {
        super.setupPlayer(player);

        // Teleport the player to their spawn point
        Location spawn = participantA.containsPlayer(player.getUniqueId()) ?
                getArena().getSpawnA() : getArena().getSpawnB();

        if (spawn.getBlock().getType() == Material.AIR) {
            player.teleport(spawn);
        } else {
            player.teleport(spawn.add(0, 2, 0));
        }
    }

    @Override
    public void end() {
        super.end();

        if (participantA.getPlayers().size() == 1 && participantB.getPlayers().size() == 1) {
            UUID rematchKey = UUID.randomUUID();

            for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
                for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                    if (!gamePlayer.isDisconnected()) {
                        Profile profile = Profile.getByUuid(gamePlayer.getUuid());

                        if (profile.getParty() == null) {
                            UUID opponent;

                            if (gameParticipant.equals(participantA)) {
                                opponent = participantB.getLeader().getUuid();
                            } else {
                                opponent = participantA.getLeader().getUuid();
                            }

                            if (opponent != null) {
                                ProfileRematchData rematchData = new ProfileRematchData(rematchKey,
                                        gamePlayer.getUuid(), opponent, kit);
                                profile.setRematchData(rematchData);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canEndMatch() {
        if (kit.getGameRules().isSumo()) {
            if (ranked) {
                return participantA.getRoundWins() == 3 || participantB.getRoundWins() == 3;
            } else {
                return participantA.getRoundWins() == 1 || participantB.getRoundWins() == 1;
            }
        } else {
            return participantA.isAllDead() || participantB.isAllDead();
        }
    }

    @Override
    public boolean canStartRound() {
        if (kit.getGameRules().isSumo()) {
            if (ranked) {
                return !(participantA.getRoundWins() == 3 || participantB.getRoundWins() == 3);
            }
        }

        return false;
    }

    @Override
    public void onRoundEnd() {
        // Store winning participant
        winningParticipant = participantA.isAllDead() ? participantB : participantA;
        winningParticipant.setRoundWins(winningParticipant.getRoundWins() + 1);

        // Store losing participant
        losingParticipant = participantA.isAllDead() ? participantA : participantB;
        losingParticipant.setEliminated(true);

        if (kit.getGameRules().isSumo()) {
            if (!canEndMatch()) {
                int roundsToWin = (ranked ? 3 : 1) - winningParticipant.getRoundWins();


            }
        } else {
            // Set opponents in snapshots if solo
            if (participantA.getPlayers().size() == 1 && participantB.getPlayers().size() == 1) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (MatchSnapshot snapshot : snapshots) {
                            if (snapshot.getUuid().equals(participantA.getLeader().getUuid())) {
                                snapshot.setOpponent(participantB.getLeader().getUuid());
                            } else if (snapshot.getUuid().equals(participantB.getLeader().getUuid())) {
                                snapshot.setOpponent(participantA.getLeader().getUuid());
                            }
                        }
                    }
                }.runTaskLater(Practice.getInstance(), 10L);


                if (ranked) {
                    int oldWinnerElo = winningParticipant.getLeader().getElo();
                    int oldLoserElo = losingParticipant.getLeader().getElo();

                    int newWinnerElo = EloUtil.getNewRating(oldWinnerElo, oldLoserElo, true);
                    int newLoserElo = EloUtil.getNewRating(oldLoserElo, oldWinnerElo, false);

                    winningParticipant.getLeader().setEloMod(newWinnerElo - oldWinnerElo);
                    losingParticipant.getLeader().setEloMod(oldLoserElo - newLoserElo);

                    Profile winningProfile = Profile.getByUuid(winningParticipant.getLeader().getUuid());
                    winningProfile.getKitData().get(getKit()).setElo(newWinnerElo);

                    Profile losingProfile = Profile.getByUuid(losingParticipant.getLeader().getUuid());
                    losingProfile.getKitData().get(getKit()).setElo(newLoserElo);
                }
            }
        }

        super.onRoundEnd();
    }

    @Override
    public boolean canEndRound() {
        return participantA.isAllDead() || participantB.isAllDead();
    }

    @Override
    public boolean isOnSameTeam(Player first, Player second) {
        boolean[] booleans = new boolean[]{
                participantA.containsPlayer(first.getUniqueId()),
                participantB.containsPlayer(first.getUniqueId()),
                participantA.containsPlayer(second.getUniqueId()),
                participantB.containsPlayer(second.getUniqueId())
        };

        return (booleans[0] && booleans[2]) || (booleans[1] && booleans[3]);
    }

    @Override
    public List<GameParticipant<MatchGamePlayer>> getParticipants() {
        return Arrays.asList(participantA, participantB);
    }

    @Override
    public org.bukkit.ChatColor getRelationColor(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return org.bukkit.ChatColor.GREEN;
        }

        boolean[] booleans = new boolean[]{
                participantA.containsPlayer(viewer.getUniqueId()),
                participantB.containsPlayer(viewer.getUniqueId()),
                participantA.containsPlayer(target.getUniqueId()),
                participantB.containsPlayer(target.getUniqueId())
        };

        if ((booleans[0] && booleans[3]) || (booleans[2] && booleans[1])) {
            return org.bukkit.ChatColor.RED;
        } else if ((booleans[0] && booleans[2]) || (booleans[1] && booleans[3])) {
            return org.bukkit.ChatColor.BLUE;
        } else if (spectators.contains(viewer.getUniqueId())) {
            return participantA.containsPlayer(target.getUniqueId()) ?
                    org.bukkit.ChatColor.BLUE : org.bukkit.ChatColor.RED;
        } else {
            return org.bukkit.ChatColor.YELLOW;
        }
    }

    @Override
    public void addSpectator(Player spectator, Player target) {
        super.addSpectator(spectator, target);

        ChatColor firstColor;
        ChatColor secondColor;

        if (participantA.containsPlayer(target.getUniqueId())) {
            firstColor = ChatColor.GREEN;
            secondColor = ChatColor.RED;
        } else {
            firstColor = ChatColor.RED;
            secondColor = ChatColor.GREEN;
        }

        if (ranked) {
            spectator.sendMessage(Locale.MATCH_START_SPECTATING_RANKED.format(spectator,
                    firstColor.toString(),
                    participantA.getConjoinedNames(),
                    participantA.getLeader().getElo(),
                    secondColor.toString(),
                    participantB.getConjoinedNames(),
                    participantB.getLeader().getElo()
            ));
        } else {
            spectator.sendMessage(Locale.MATCH_START_SPECTATING.format(spectator,
                    firstColor.toString(),
                    participantA.getConjoinedNames(),
                    secondColor.toString(),
                    participantB.getConjoinedNames()
            ));
        }
    }

    @Override
    public void sendEndMessage(Player player) {
        List<String> formattedStrings = new ArrayList<>(Locale.MATCH_END_DETAILS.formatLines());
        for (String string : formattedStrings) {
            if (string.equalsIgnoreCase("%INVENTORIES%")) {
                ChatComponentBuilder winner = new ChatComponentBuilder(Locale.MATCH_END_WINNER_INVENTORY
                        .format(player));

                ChatComponentBuilder separator = new ChatComponentBuilder("&7 | ");

                ChatComponentBuilder loser = new ChatComponentBuilder(Locale.MATCH_END_LOSER_INVENTORY
                        .format(player));

                PlayerUtil.sendMessage(player,
                        Collections.singletonList(winner).toArray(new ChatComponentBuilder[0]),
                        getTeamAsComponent(winningParticipant),
                        Collections.singletonList(separator).toArray(new ChatComponentBuilder[0]),
                        Collections.singletonList(loser).toArray(new ChatComponentBuilder[0]),
                        getTeamAsComponent(losingParticipant));

            } else if (string.equalsIgnoreCase("%ENDMESSAGE%")) {
                ChatComponentBuilder replay = new ChatComponentBuilder(Locale.MATCH_SHOW_REPLAY_RECEIVED_CLICKABLE.format(player));
                replay.attachToEachPart(ChatHelper.click("/replay play " + player.getUniqueId()));
                replay.attachToEachPart(ChatHelper.hover(Locale.MATCH_SHOW_REPLAY_HOVER.format(player)));

                ChatComponentBuilder rematch = new ChatComponentBuilder(Locale.REMATCH_SHOW_REPLAY_RECEIVED_CLICKABLE.format(player));
                rematch.attachToEachPart(ChatHelper.click("/rematch " + player.getName()));
                rematch.attachToEachPart(ChatHelper.hover(Locale.REMATCH_SHOW_REPLAY_HOVER.format(player)));


                if (Practice.getInstance().isReplay() && !kit.getGameRules().isBuild()) {
                    PlayerUtil.sendMessage(player,
                            Collections.singletonList(replay).toArray(new ChatComponentBuilder[0]),
                            Collections.singletonList(rematch).toArray(new ChatComponentBuilder[0]));
                } else {
                    PlayerUtil.sendMessage(player,
                            Collections.singletonList(rematch).toArray(new ChatComponentBuilder[0]));

                }

            } else if (string.equalsIgnoreCase("%ELO_CHANGES%")) {
                if (participantA.getPlayers().size() == 1 && participantB.getPlayers().size() == 1 && ranked) {
                    List<String> sectionLines = Locale.MATCH_ELO_CHANGES.formatLines(
                            winningParticipant.getConjoinedNames(),
                            (winningParticipant.getLeader().getEloMod()),
                            (winningParticipant.getLeader().getElo() + winningParticipant.getLeader().getEloMod()),
                            (losingParticipant.getConjoinedNames()),
                            (losingParticipant.getLeader().getEloMod()),
                            (losingParticipant.getLeader().getElo() - losingParticipant.getLeader().getEloMod())
                    );
                    for (String sectionLine : sectionLines) {
                        player.sendMessage(CC.translate(sectionLine));
                    }
                }
            } else {
                player.sendMessage(CC.translate(string));
            }
        }
    }


}
