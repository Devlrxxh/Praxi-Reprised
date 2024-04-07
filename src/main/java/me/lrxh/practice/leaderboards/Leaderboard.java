package me.lrxh.practice.leaderboards;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.profile.Profile;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
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
        List<PlayerElo> topEloPlayers = Profile.collection.find().into(new ArrayList<>()).stream()
                .map(profileDocument -> mapToPlayerElo(profileDocument, queue))
                .sorted(Comparator.comparingInt(PlayerElo::getElo).reversed()) // Sort by elo
                .limit(10)
                .collect(Collectors.toList());

        List<PlayerElo> topKillPlayers = Profile.collection.find().into(new ArrayList<>()).stream()
                .map(profileDocument -> mapToPlayerElo(profileDocument, queue))
                .sorted(Comparator.comparingInt(PlayerElo::getKills).reversed()) // Sort by kills
                .limit(10)
                .collect(Collectors.toList());

        for (int i = topEloPlayers.size(); i < 10; i++) {
            topEloPlayers.add(new PlayerElo("???", 0, 0, 0));
        }

        for (int i = topKillPlayers.size(); i < 10; i++) {
            topKillPlayers.add(new PlayerElo("???", 0, 0, 0));
        }

        return new QueueLeaderboard(queue, topEloPlayers, topKillPlayers);
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
        return queueStats != null ? queueStats.getInteger("elo") : 1000;
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
