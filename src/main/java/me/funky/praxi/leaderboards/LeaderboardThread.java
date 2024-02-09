package me.funky.praxi.leaderboards;

import me.funky.praxi.Praxi;

public class LeaderboardThread extends Thread {
    private static final long SECOND_IN_MILLIS = 1000L;

    @Override
    public void run() {
        long refreshTimeInterval = Praxi.getInstance().getMainConfig().getInteger("LEADERBOARD.UPDATE-TIME") * SECOND_IN_MILLIS * 60;
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
                    Leaderboard.getEloPositions().clear();
                    Leaderboard.setEloPositions(Leaderboard.init());
                }
                sleep(SECOND_IN_MILLIS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {
            }
        }
    }
}
