package me.lrxh.practice;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.queue.QueueProfile;
import me.lrxh.practice.util.LocationUtil;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Cache {
    private final List<Queue> queues = new ArrayList<>();
    private final LinkedList<QueueProfile> players = new LinkedList<>();
    private final List<Match> matches = new ArrayList<>();
    private Location spawn = LocationUtil.deserialize(Practice.getInstance().getMainConfig().getStringOrDefault("ESSENTIAL.SPAWN_LOCATION", null));

    public Match getMatch(UUID matchUUID) {
        for (Match match : matches) {
            if (match.getMatchId().equals(matchUUID)) {
                return match;
            }
        }
        return null;
    }
}
