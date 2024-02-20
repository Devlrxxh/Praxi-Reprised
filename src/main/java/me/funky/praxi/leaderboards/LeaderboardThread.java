package me.funky.praxi.leaderboards;

import me.funky.praxi.Practice;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.Console;
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
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(CC.translate("&aLeaderboards Refreshed!"));
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
