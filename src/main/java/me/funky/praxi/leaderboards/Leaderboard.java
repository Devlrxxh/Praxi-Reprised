package me.funky.praxi.leaderboards;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.profile.Profile;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

public class Leaderboard {
    @Getter
    @Setter
    private static Map<String, QueueLeaderboard> eloLeaderboards = init();

    @Getter
    @Setter
    private static long refreshTime;

    public static Map<String, QueueLeaderboard> init() {
        Map<String, QueueLeaderboard> leaderboards = new HashMap<>();
        for (Kit kit : Kit.getKits()) {
            leaderboards.put(kit.getName(), initQueueLeaderboard(kit.getName()));
        }

        return leaderboards;
    }

    private static QueueLeaderboard initQueueLeaderboard(String queue) {
        List<PlayerElo> topPlayers = Profile.collection.find().into(new ArrayList<>()).stream()
                .map(profileDocument -> mapToPlayerElo(profileDocument, queue))
                .sorted(Comparator.reverseOrder())
                .limit(10)
                .collect(Collectors.toList());

        for (int i = topPlayers.size(); i < 10; i++) {
            topPlayers.add(new PlayerElo("???", 0, 0, 0));
        }

        return new QueueLeaderboard(queue, topPlayers);
    }


    private static PlayerElo mapToPlayerElo(Document profileDocument, String queue) {
        return new PlayerElo(profileDocument.getString("username"),
                getElo(profileDocument, queue),
                getKills(profileDocument, queue),
                getLoses(profileDocument, queue));
    }

    private static int getElo(Document profileDocument, String queue) {
        Document kitStatistics = (Document) profileDocument.get("kitStatistics");
        Document queueStats = (Document) kitStatistics.get(queue);
        return queueStats != null ? queueStats.getInteger("elo") : 0;
    }

    private static int getKills(Document profileDocument, String queue) {
        Document kitStatistics = (Document) profileDocument.get("kitStatistics");
        Document queueStats = (Document) kitStatistics.get(queue);
        return queueStats != null ? queueStats.getInteger("won") : 0;
    }

    private static int getLoses(Document profileDocument, String queue) {
        Document kitStatistics = (Document) profileDocument.get("kitStatistics");
        Document queueStats = (Document) kitStatistics.get(queue);
        return queueStats != null ? queueStats.getInteger("lost") : 0;
    }
}
