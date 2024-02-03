package me.funky.praxi.queue;

import me.funky.praxi.Locale;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.impl.BasicTeamMatch;
import me.funky.praxi.match.participant.MatchGamePlayer;
import me.funky.praxi.participant.GameParticipant;
import me.funky.praxi.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class QueueThread extends Thread {

    @Override
    public void run() {
        while (true) {
            try {
                for (QueueProfile queueProfile : Praxi.getInstance().getCache().getPlayers()) {
                    Praxi.getInstance().getCache().getPlayers().forEach(QueueProfile::tickRange);

                    if (Praxi.getInstance().getCache().getPlayers().size() < 2) {
                        continue;
                    }

                    for (QueueProfile firstQueueProfile : Praxi.getInstance().getCache().getPlayers()) {
                        Player firstPlayer = Bukkit.getPlayer(firstQueueProfile.getPlayerUuid());

                        if (firstPlayer == null) {
                            continue;
                        }

                        for (QueueProfile secondQueueProfile : Praxi.getInstance().getCache().getPlayers()) {
                            if (secondQueueProfile.getPlayerUuid() == firstQueueProfile.getPlayerUuid()) break;

                            if (!firstQueueProfile.areSame(secondQueueProfile)) {
                                break;
                            }

                            Player secondPlayer = Bukkit.getPlayer(secondQueueProfile.getPlayerUuid());

                            if (secondPlayer == null) {
                                continue;
                            }

//							if (firstProfile.getOptions().isUsingPingFactor() ||
//							    secondProfile.getOptions().isUsingPingFactor()) {
//								if (firstPlayer.getPing() >= secondPlayer.getPing()) {
//									if (firstPlayer.getPing() - secondPlayer.getPing() >= 50) {
//										continue;
//									}
//								} else {
//									if (secondPlayer.getPing() - firstPlayer.getPing() >= 50) {
//										continue;
//									}
//								}
//							}

                            if (queueProfile.isRanked()) {
                                if (firstQueueProfile.isInRange(secondQueueProfile.getElo()) ||
                                        secondQueueProfile.isInRange(firstQueueProfile.getElo())) {
                                    continue;
                                }
                            }

                            // Find arena
                            final Arena arena = Arena.getRandomArena(queueProfile.getQueue().getKit());

                            if (arena == null) {
                                continue;
                            }

                            // Update arena
                            arena.setActive(true);

                            // Remove players from queue
                            Praxi.getInstance().getCache().getPlayers().remove(firstQueueProfile);
                            Praxi.getInstance().getCache().getPlayers().remove(secondQueueProfile);

                            MatchGamePlayer playerA = new MatchGamePlayer(firstPlayer.getUniqueId(),
                                    firstPlayer.getName(), firstQueueProfile.getElo());

                            MatchGamePlayer playerB = new MatchGamePlayer(secondPlayer.getUniqueId(),
                                    secondPlayer.getName(), secondQueueProfile.getElo());

                            GameParticipant<MatchGamePlayer> participantA = new GameParticipant<>(playerA);
                            GameParticipant<MatchGamePlayer> participantB = new GameParticipant<>(playerB);

                            // Create match
                            Match match = new BasicTeamMatch(queueProfile.getQueue(), queueProfile.getQueue().getKit(), arena, queueProfile.isRanked(),
                                    participantA, participantB);


                                    for (String line : Locale.MATCH_START.formatLines(secondPlayer.getName(), queueProfile.getQueue().getKit().getName(), PlayerUtil.getPing(secondPlayer))) {
                                        firstPlayer.sendMessage(line);
                                    }
                            for (String line : Locale.MATCH_START.formatLines(firstPlayer.getName(), queueProfile.getQueue().getKit().getName(), PlayerUtil.getPing(firstPlayer))) {
                                secondPlayer.sendMessage(line);
                            }
                                        match.getKit().removeQueue((byte) 2);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    match.start();
                                }
                            }.runTask(Praxi.getInstance());
                        }
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ignored) {
                }

                continue;
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
