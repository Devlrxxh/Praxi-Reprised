package me.lrxh.practice.leaderboards;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerElo implements Comparable<PlayerElo> {
    private final String playerName;
    private final int elo;
    private final int kills;
    private final int loses;


    @Override
    public int compareTo(PlayerElo other) {
        return Integer.compare(this.elo, other.elo);
    }
}
