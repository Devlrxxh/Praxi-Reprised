package me.funky.praxi.leaderboards;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.profile.Profile;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Leaderboard {
    @Getter
    @Setter
    private static List<Positions> eloPositions = init();
    @Getter
    @Setter
    private static long refreshTime;
    public static List<Positions> init() {
        List<PlayerElo> topPlayers = Profile.collection.find().into(new ArrayList<>()).stream()
                .map(Leaderboard::mapToPlayerElo)
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .collect(Collectors.toList());

        return IntStream.range(0, 50)
                .mapToObj(i -> {
                    if (i < topPlayers.size()) {
                        return new Positions(i + 1, topPlayers.get(i));
                    } else {
                        return new Positions(i + 1, new PlayerElo("none", 0, 0, 0));
                    }
                })
                .collect(Collectors.toList());
    }

    private static PlayerElo mapToPlayerElo(Document profileDocument) {
        return new PlayerElo(profileDocument.getString("username"), getElo(profileDocument), getKills(profileDocument), getLoses(profileDocument));
    }

    private static int getElo(Document profileDocument) {
        Document kitStatistics = (Document) profileDocument.get("kitStatistics");
        int totalQueue = kitStatistics.keySet().size();
        if (totalQueue == 0) {
            return 0;
        }

        return kitStatistics.values().stream()
                .mapToInt(kit -> ((Document) kit).getInteger("elo"))
                .sum() / totalQueue;
    }
    private static int getKills(Document profileDocument) {
        int kills = 0;
        Document kitStatistics = (Document) profileDocument.get("kitStatistics");

        for (String key : kitStatistics.keySet()) {
            Document kitDocument = (Document) kitStatistics.get(key);
            kills += kitDocument.getInteger("won");
        }
        return kills;
    }

    private static int getLoses(Document profileDocument) {
        int loses = 0;
        Document kitStatistics = (Document) profileDocument.get("kitStatistics");

        for (String key : kitStatistics.keySet()) {
            Document kitDocument = (Document) kitStatistics.get(key);
            loses += kitDocument.getInteger("lost");
        }
        return loses;
    }
}
