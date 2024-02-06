package me.funky.praxi.leaderboards;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerElo implements Comparable<PlayerElo> {
    private final String playerName;
    private final int elo;


    @Override
    public int compareTo(PlayerElo other) {
        return Integer.compare(this.elo, other.elo);
    }
}
