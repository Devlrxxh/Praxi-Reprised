package me.funky.praxi.leaderboards;

public class LeaderboardThread extends Thread{
    @Override
    public void run() {
        while (true) {
            try{
                Leaderboard.init();
            } catch (Exception e) {
                try {
                    Thread.sleep(900000L);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
