package me.funky.praxi.event.game.map;


import me.funky.praxi.util.command.command.adapter.CommandTypeAdapter;

public class EventGameMapTypeAdapter implements CommandTypeAdapter {

	@Override
	public <T> T convert(String string, Class<T> type) {
		return type.cast(EventGameMap.getByName(string));
	}

}

