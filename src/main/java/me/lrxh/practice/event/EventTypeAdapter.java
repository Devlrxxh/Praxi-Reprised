package me.lrxh.practice.event;

import me.lrxh.practice.event.impl.sumo.SumoEvent;
import me.lrxh.practice.util.command.command.adapter.CommandTypeAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventTypeAdapter implements CommandTypeAdapter {

    private final static Map<String, Class<? extends Event>> map;

    static {
        map = new HashMap<>();
        map.put("sumo", SumoEvent.class);
    }

    @Override
    public <T> T convert(String string, Class<T> type) {
        return type.cast(Event.getEvent(map.get(string.toLowerCase())));
    }

    @Override
    public <T> List<String> tabComplete(String string, Class<T> type) {
        return Collections.singletonList("Sumo");
    }

}
