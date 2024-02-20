package me.lrxh.practice.kit;


import me.lrxh.practice.util.command.command.adapter.CommandTypeAdapter;

public class KitTypeAdapter implements CommandTypeAdapter {

    @Override
    public <T> T convert(String string, Class<T> type) {
        return type.cast(Kit.getByName(string));
    }

}
