package me.lrxh.practice.leaderboards;

import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.Console;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LeaderboardThread extends Thread {
    private static final long SECOND_IN_MILLIS = 1000L;

    @Override
    public void run() {
        long refreshTimeInterval = Practice.getInstance().getMainConfig().getInteger("LEADERBOARD.UPDATE-TIME") * SECOND_IN_MILLIS * 60;
        boolean rested = false;

        while (!isInterrupted()) {
            try {
                if (!rested) {
                    Leaderboard.setRefreshTime(refreshTimeInterval);
                    rested = true;
                }

                sleep(SECOND_IN_MILLIS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                try {
                    sleep(refreshTimeInterval);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }

            try {
                Leaderboard.setRefreshTime(Leaderboard.getRefreshTime() - SECOND_IN_MILLIS);

                if (Leaderboard.getRefreshTime() <= 0) {
                    rested = false;
                    for (Profile profile : Profile.getProfiles().values()) {
                        profile.save();
                    }
                    Console.sendMessage(CC.translate("&aSaved Player Data!"));
                    Leaderboard.getEloLeaderboards().clear();
                    Leaderboard.setEloLeaderboards(Leaderboard.init());
                    if (Practice.getInstance().getMainConfig().getBoolean("LEADERBOARD.ENABLE-MESSAGE")) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendMessage(Locale.LEADERBOARD_REFRESH.format(player));
                        }
                    }
                }
                sleep(SECOND_IN_MILLIS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {
            }
        }
    }
}
