package me.funky.praxi.util.command.command.adapter.impl;

import me.funky.praxi.util.command.command.adapter.CommandTypeAdapter;
import org.bukkit.Bukkit;

public class WorldTypeAdapter implements CommandTypeAdapter {
    @Override
    public <T> T convert(final String string, final Class<T> type) {
        return type.cast(Bukkit.getWorld(string));
    }
}
