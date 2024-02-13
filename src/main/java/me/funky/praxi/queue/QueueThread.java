package me.funky.praxi.queue;

import me.funky.praxi.Locale;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.impl.BasicTeamMatch;
import me.funky.praxi.match.participant.MatchGamePlayer;
import me.funky.praxi.participant.GameParticipant;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.BukkitReflection;
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
                            secondQueueProfile.getQueue().getKit().removeQueue();
                            firstQueueProfile.getQueue().getKit().removeQueue();

                            MatchGamePlayer playerA = new MatchGamePlayer(firstPlayer.getUniqueId(),
                                    firstPlayer.getName(), firstQueueProfile.getElo());

                            MatchGamePlayer playerB = new MatchGamePlayer(secondPlayer.getUniqueId(),
                                    secondPlayer.getName(), secondQueueProfile.getElo());

                            GameParticipant<MatchGamePlayer> participantA = new GameParticipant<>(playerA);
                            GameParticipant<MatchGamePlayer> participantB = new GameParticipant<>(playerB);

                            // Create match
                            Match match = new BasicTeamMatch(queueProfile.getQueue(), queueProfile.getQueue().getKit(), arena, queueProfile.isRanked(),
                                    participantA, participantB);


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
