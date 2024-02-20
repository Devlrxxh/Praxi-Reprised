package me.lrxh.practice;

import lombok.Getter;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.queue.QueueProfile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public class Cache {
    private final List<Queue> queues = new ArrayList<>();
    private final LinkedList<QueueProfile> players = new LinkedList<>();
    private final List<Match> matches = new ArrayList<>();
}
