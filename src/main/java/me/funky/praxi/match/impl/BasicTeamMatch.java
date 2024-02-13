package me.funky.praxi.match.impl;

import lombok.Getter;
import me.funky.praxi.Locale;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.MatchSnapshot;
import me.funky.praxi.match.MatchState;
import me.funky.praxi.match.participant.MatchGamePlayer;
import me.funky.praxi.participant.GameParticipant;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.meta.ProfileRematchData;
import me.funky.praxi.queue.Queue;
import me.funky.praxi.util.ChatComponentBuilder;
import me.funky.praxi.util.TimeUtil;
import me.funky.praxi.util.elo.EloUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
public class BasicTeamMatch extends Match {

    private final GameParticipant<MatchGamePlayer> participantA;
    private final GameParticipant<MatchGamePlayer> participantB;
    private GameParticipant<MatchGamePlayer> winningParticipant;
    private GameParticipant<MatchGamePlayer> losingParticipant;

    public BasicTeamMatch(Queue queue, Kit kit, Arena arena, boolean ranked, GameParticipant<MatchGamePlayer> participantA,
                          GameParticipant<MatchGamePlayer> participantB) {
        super(queue, kit, arena, ranked);

        this.participantA = participantA;
        this.participantB = participantB;
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
                }.runTaskLater(Praxi.getInstance(), 10L);


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
            return org.bukkit.ChatColor.GREEN;
        } else if (spectators.contains(viewer.getUniqueId())) {
            return participantA.containsPlayer(target.getUniqueId()) ?
                    org.bukkit.ChatColor.GREEN : org.bukkit.ChatColor.RED;
        } else {
            return org.bukkit.ChatColor.YELLOW;
        }
    }

    @Override
    public List<String> getScoreboardLines(Player player) {
        List<String> lines = new ArrayList<>();

        if (getParticipant(player) != null) {
            if (state == MatchState.STARTING_ROUND || state == MatchState.PLAYING_ROUND) {
                if (participantA.getPlayers().size() == 1 && participantB.getPlayers().size() == 1) {
                    GameParticipant<MatchGamePlayer> opponent;

                    if (participantA.containsPlayer(player.getUniqueId())) {
                        opponent = participantB;
                    } else {
                        opponent = participantA;
                    }

                    lines.add("&cDuration: &r" + getDuration());
                    lines.add("&cOpponent: &r" + opponent.getConjoinedNames());
                } else {
                    GameParticipant<MatchGamePlayer> friendly = getParticipant(player);
                    GameParticipant<MatchGamePlayer> opponent = participantA.equals(friendly) ?
                            participantB : participantA;

                    if (friendly.getPlayers().size() + opponent.getPlayers().size() <= 6) {
                        lines.add("&cDuration: &7" + getDuration());
                        lines.add("");
                        lines.add("&aTeam &a(" + friendly.getAliveCount() + "/" + friendly.getPlayers().size() + ")");

                        for (MatchGamePlayer gamePlayer : friendly.getPlayers()) {
                            lines.add(" " + (gamePlayer.isDead() || gamePlayer.isDisconnected() ? "&7&m" : "") +
                                    gamePlayer.getUsername());
                        }

                        lines.add("");
                        lines.add("&cOpponents &c(" + opponent.getAliveCount() + "/" + opponent.getPlayers().size() +
                                ")");

                        for (MatchGamePlayer gamePlayer : opponent.getPlayers()) {
                            lines.add(" " + (gamePlayer.isDead() || gamePlayer.isDisconnected() ? "&7&m" : "") +
                                    gamePlayer.getUsername());
                        }
                    } else {
                        lines.add("&cDuration: &7" + getDuration());
                        lines.add("&aTeam: &7" + friendly.getAliveCount() + "/" + friendly.getPlayers().size());
                        lines.add("&cOpponents: &7" + opponent.getAliveCount() + "/" + opponent.getPlayers().size());
                    }
                }
            } else {
                lines.add("&cDuration: &7" + TimeUtil.millisToTimer(timeData));
            }
        } else {
            lines.add("&cKit: &7" + getKit().getName());
            lines.add("&cDuration: &7" + getDuration());
            lines.add("");

            if (participantA.getPlayers().size() <= 2 && participantB.getPlayers().size() <= 2) {
                for (MatchGamePlayer gamePlayer : participantA.getPlayers()) {
                    lines.add(gamePlayer.getUsername());
                }

                lines.add("vs");

                for (MatchGamePlayer gamePlayer : participantB.getPlayers()) {
                    lines.add(gamePlayer.getUsername());
                }
            } else {
                lines.add(participantA.getLeader().getUsername() + "'s Team");
                lines.add(participantB.getLeader().getUsername() + "'s Team");
            }
        }

        return lines;
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
    public List<BaseComponent[]> generateEndComponents() {
        List<BaseComponent[]> componentsList = new ArrayList<>();

        for (String line : Locale.MATCH_END_DETAILS.formatLines()) {
            if (line.equalsIgnoreCase("%INVENTORIES%")) {
                BaseComponent[] winners = generateInventoriesComponents(
                        Locale.MATCH_END_WINNER_INVENTORY.format(participantA.getPlayers().size() == 1 ? "" : "s"),
                        winningParticipant);

                BaseComponent[] losers = generateInventoriesComponents(
                        Locale.MATCH_END_LOSER_INVENTORY.format(participantB.getPlayers().size() == 1 ? "" : "s"),
                        losingParticipant);

                if (participantA.getPlayers().size() == 1 && participantB.getPlayers().size() == 1) {
                    ChatComponentBuilder builder = new ChatComponentBuilder("");

                    for (BaseComponent component : winners) {
                        builder.append((TextComponent) component);
                    }

                    builder.append(new ChatComponentBuilder("&7 - ").create());

                    for (BaseComponent component : losers) {
                        builder.append((TextComponent) component);
                    }

                    componentsList.add(builder.create());
                } else {
                    componentsList.add(winners);
                    componentsList.add(losers);
                }

                continue;
            }

            if (line.equalsIgnoreCase("%ELO_CHANGES%")) {
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
                        componentsList.add(new ChatComponentBuilder("").parse(sectionLine).create());
                    }
                }

                continue;
            }

            componentsList.add(new ChatComponentBuilder("").parse(line).create());
        }

        return componentsList;
    }

}
