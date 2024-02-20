package me.lrxh.practice.leaderboards;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Positions {
    private final int rank;
    private final PlayerElo playerElo;

    @Override
    public String toString() {
        return rank + ". " + playerElo.getPlayerName() + ", ELO: " + playerElo.getElo();
    }
}
