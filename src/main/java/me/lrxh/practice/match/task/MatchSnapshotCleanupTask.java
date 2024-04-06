package me.lrxh.practice.match.task;

import me.lrxh.practice.match.MatchSnapshot;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchSnapshotCleanupTask extends BukkitRunnable {

    @Override
    public void run() {
        MatchSnapshot.getSnapshots().entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().getCreatedAt() >= 45_000);
    }

}
