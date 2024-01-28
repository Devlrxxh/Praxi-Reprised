package me.funky.praxi.queue;

import me.funky.praxi.Praxi;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.impl.BasicTeamMatch;
import me.funky.praxi.match.participant.MatchGamePlayer;
import me.funky.praxi.participant.GameParticipant;
import me.funky.praxi.util.CC;
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
                                if (!firstQueueProfile.isInRange(secondQueueProfile.getElo()) ||
                                        !secondQueueProfile.isInRange(firstQueueProfile.getElo())) {
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

                            String[] opponentMessages = formatMessages(firstPlayer.getName(),
                                    secondPlayer.getName(), firstQueueProfile.getElo(), secondQueueProfile.getElo(),
                                    queueProfile.isRanked());

                            firstPlayer.sendMessage(opponentMessages[0]);
                            secondPlayer.sendMessage(opponentMessages[1]);

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
                e.printStackTrace();

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }

                continue;
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] formatMessages(String player1, String player2, int player1Elo, int player2Elo, boolean ranked) {
        String player1Format = player1 + (ranked ? CC.PINK + " (" + player1Elo + ")" : "");
        String player2Format = player2 + (ranked ? CC.PINK + " (" + player2Elo + ")" : "");

        return new String[]{
                CC.YELLOW + CC.BOLD + "Found opponent: " + CC.GREEN + player1Format + CC.YELLOW + " vs. " +
                        CC.RED + player2Format,
                CC.YELLOW + CC.BOLD + "Found opponent: " + CC.GREEN + player2Format + CC.YELLOW + " vs. " +
                        CC.RED + player1Format
        };
    }

}
