package me.funky.praxi;

import lombok.Getter;
import me.funky.praxi.match.Match;
import me.funky.praxi.queue.Queue;
import me.funky.praxi.queue.QueueProfile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public class Cache {
    private final List<Queue> queues = new ArrayList<>();
    private final LinkedList<QueueProfile> players = new LinkedList<>();
    private final List<Match> matches = new ArrayList<>();
}
