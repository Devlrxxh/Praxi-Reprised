package me.lrxh.practice.util.command.command.adapter.impl;

import me.lrxh.practice.util.command.command.CommandOption;
import me.lrxh.practice.util.command.command.adapter.CommandTypeAdapter;

public class CommandOptionTypeAdapter implements CommandTypeAdapter {
    @Override
    public <T> T convert(final String string, final Class<T> type) {
        if (string.startsWith("-")) {
            return type.cast(new CommandOption(string.substring(1)));
        }
        return null;
    }
}
