package me.funky.praxi.leaderboards;

import com.mongodb.client.FindIterable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.funky.praxi.profile.Profile;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Leaderboard {
    @Getter
    private static final List<Positions> topPositions = init();

    public static List<Positions> init() {
        FindIterable<Document> profileDocuments = Profile.collection.find();
        List<PlayerElo> topPlayers = profileDocuments.into(new ArrayList<>()).stream()
                .map(Leaderboard::mapToPlayerElo)
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .collect(Collectors.toList());

        return IntStream.range(0, topPlayers.size())
                .mapToObj(i -> new Positions(i + 1, topPlayers.get(i)))
                .collect(Collectors.toList());
    }

    private static PlayerElo mapToPlayerElo(Document profileDocument) {
        String playerName = profileDocument.getString("username");
        int elo = getElo(profileDocument);
        return new PlayerElo(playerName, elo);
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
}
