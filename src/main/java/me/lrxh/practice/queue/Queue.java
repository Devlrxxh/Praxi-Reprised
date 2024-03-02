package me.lrxh.practice.queue;

import lombok.Getter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class Queue {


    private final UUID uuid = UUID.randomUUID();
    private final Kit kit;
    private final boolean ranked;
    private int queuing;

    public Queue(Kit kit, boolean ranked) {
        this.kit = kit;
        this.ranked = ranked;
        this.queuing = 0;
        Practice.getInstance().getCache().getQueues().add(this);
    }

    public static Queue getByUuid(UUID uuid) {
        for (Queue queue : Practice.getInstance().getCache().getQueues()) {
            if (queue.getUuid().equals(uuid)) {
                return queue;
            }
        }

        return null;
    }

    public String getQueueName() {
        return (ranked ? "Ranked" : "Unranked") + " " + kit.getName();
    }

    public void addPlayer(Player player, int elo, boolean ranked) {
        QueueProfile queueProfile = new QueueProfile(this, player.getUniqueId(), ranked);
        queueProfile.setElo(elo);

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setQueueProfile(queueProfile);
        profile.setState(ProfileState.QUEUEING);

        Practice.getInstance().getCache().getPlayers().add(queueProfile);

        Practice.getInstance().getHotbar().giveHotbarItems(player);

        if (ranked) {
            player.sendMessage(Locale.QUEUE_JOIN_RANKED.format(player, kit.getName(), elo));
        } else {
            player.sendMessage(Locale.QUEUE_JOIN_UNRANKED.format(player, kit.getName()));
        }
    }


    public void removePlayer(QueueProfile queueProfile) {
        Practice.getInstance().getCache().getPlayers().remove(queueProfile);

        Profile profile = Profile.getByUuid(queueProfile.getPlayerUuid());
        profile.setQueueProfile(null);
        profile.setState(ProfileState.LOBBY);

        Player player = Bukkit.getPlayer(queueProfile.getPlayerUuid());

        if (player != null) {
            Practice.getInstance().getHotbar().giveHotbarItems(player);

            if (ranked) {
                player.sendMessage(Locale.QUEUE_LEAVE_RANKED.format(player, kit.getName()));
            } else {
                player.sendMessage(Locale.QUEUE_LEAVE_UNRANKED.format(player, kit.getName()));
            }
            removeQueue();
        }

    }

    public void addQueue() {
        queuing += 1;
    }

    public void removeQueue() {
        queuing -= 1;
    }
}
