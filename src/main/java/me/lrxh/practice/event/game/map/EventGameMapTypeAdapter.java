package me.lrxh.practice.event.game.map;


import me.lrxh.practice.util.command.command.adapter.CommandTypeAdapter;

public class EventGameMapTypeAdapter implements CommandTypeAdapter {

    @Override
    public <T> T convert(String string, Class<T> type) {
        return type.cast(EventGameMap.getByName(string));
    }

}

