package me.lrxh.practice.leaderboards;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class QueueLeaderboard {
    private String queue;
    private List<PlayerElo> topEloPlayers;
    private List<PlayerElo> topKillPlayers;
}
