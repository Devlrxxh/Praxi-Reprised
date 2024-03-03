package me.lrxh.practice.queue;

import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.impl.BasicTeamMatch;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.BukkitReflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class QueueThread extends Thread {

    @Override
    public void run() {
        while (true) {
            try {
                for (QueueProfile queueProfile : Practice.getInstance().getCache().getPlayers()) {
                    Practice.getInstance().getCache().getPlayers().forEach(QueueProfile::tickRange);

                    if (Practice.getInstance().getCache().getPlayers().size() < 2) {
                        continue;
                    }

                    for (QueueProfile firstQueueProfile : Practice.getInstance().getCache().getPlayers()) {
                        Player firstPlayer = Bukkit.getPlayer(firstQueueProfile.getPlayerUuid());

                        if (firstPlayer == null) {
                            continue;
                        }

                        for (QueueProfile secondQueueProfile : Practice.getInstance().getCache().getPlayers()) {
                            if (secondQueueProfile.getPlayerUuid() == firstQueueProfile.getPlayerUuid()) break;

                            if (!firstQueueProfile.areSame(secondQueueProfile)) {
                                break;
                            }

                            Player secondPlayer = Bukkit.getPlayer(secondQueueProfile.getPlayerUuid());

                            if (secondPlayer == null) {
                                continue;
                            }

                            Profile firstProfile = Profile.getByUuid(firstPlayer.getUniqueId());
                            Profile secondProfile = Profile.getByUuid(secondPlayer.getUniqueId());

                            int firstPlayerPing = BukkitReflection.getPing(firstPlayer);
                            int secondPlayerPing = BukkitReflection.getPing(secondPlayer);

                            if (!(secondPlayerPing <= firstProfile.getOptions().pingRange() &&
                                    firstPlayerPing <= secondProfile.getOptions().pingRange())) {
                                break;
                            }

                            if (queueProfile.isRanked()) {
                                if (firstQueueProfile.isInRange(secondQueueProfile.getElo()) ||
                                        secondQueueProfile.isInRange(firstQueueProfile.getElo())) {
                                    continue;
                                }
                            }


                            //if (firstProfile.getOptions().eu() != secondProfile.getOptions().eu()) {
                            //    break;
                            //}

                            // Find arena
                            final Arena arena = Arena.getRandomArena(queueProfile.getQueue().getKit());

                            if (arena == null) {
                                continue;
                            }

                            // Update arena
                            arena.setActive(true);

                            // Remove players from queue
                            Practice.getInstance().getCache().getPlayers().remove(firstQueueProfile);
                            Practice.getInstance().getCache().getPlayers().remove(secondQueueProfile);
                            secondQueueProfile.getQueue().removeQueue();
                            firstQueueProfile.getQueue().removeQueue();

                            MatchGamePlayer playerA = new MatchGamePlayer(firstPlayer.getUniqueId(),
                                    firstPlayer.getName(), firstQueueProfile.getElo());

                            MatchGamePlayer playerB = new MatchGamePlayer(secondPlayer.getUniqueId(),
                                    secondPlayer.getName(), secondQueueProfile.getElo());

                            GameParticipant<MatchGamePlayer> participantA = new GameParticipant<>(playerA);
                            GameParticipant<MatchGamePlayer> participantB = new GameParticipant<>(playerB);

                            // Create match
                            Match match = new BasicTeamMatch(queueProfile.getQueue(), queueProfile.getQueue().getKit(), arena, queueProfile.isRanked(),
                                    participantA, participantB, false);


                            for (String line : Locale.MATCH_START.formatLines(firstPlayer, secondPlayer.getName(), queueProfile.getQueue().getKit().getName(), BukkitReflection.getPing(secondPlayer))) {
                                firstPlayer.sendMessage(line);
                            }
                            for (String line : Locale.MATCH_START.formatLines(secondPlayer, firstPlayer.getName(), queueProfile.getQueue().getKit().getName(), BukkitReflection.getPing(firstPlayer))) {
                                secondPlayer.sendMessage(line);
                            }

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    match.start();
                                }
                            }.runTask(Practice.getInstance());
                        }
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignored) {
                }

                continue;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
