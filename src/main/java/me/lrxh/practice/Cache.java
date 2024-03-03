package me.lrxh.practice;

import lombok.Getter;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.queue.QueueProfile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
public class Cache {
    private final List<Queue> queues = new ArrayList<>();
    private final LinkedList<QueueProfile> players = new LinkedList<>();
    private final List<Match> matches = new ArrayList<>();

    public Match getMatch(UUID matchUUID){
        for(Match match : matches){
            if(match.getMatchId().equals(matchUUID)){
                return match;
            }
        }
        return null;
    }
}
