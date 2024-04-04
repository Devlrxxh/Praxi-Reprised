package me.lrxh.practice.arena.impl;

import lombok.Getter;
import me.lrxh.practice.Practice;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.arena.ArenaType;
import me.lrxh.practice.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class StandaloneArena extends Arena {

    private final List<Arena> duplicates = new ArrayList<>();

    public StandaloneArena(String name, Location location1, Location location2) {
        super(name, location1, location2);
    }

    @Override
    public ArenaType getType() {
        return ArenaType.STANDALONE;
    }

    @Override
    public void save() {
        System.out.println("STANDALONE ARENA SAVE");
        String path = "arenas." + getName();

        FileConfiguration configuration = Practice.getInstance().getArenasConfig().getConfiguration();
        configuration.set(path, null);
        configuration.set(path + ".type", getType().name());
        configuration.set(path + ".spawnA", LocationUtil.serialize(spawnA));
        configuration.set(path + ".displayName", displayName);
        configuration.set(path + ".spawnB", LocationUtil.serialize(spawnB));
        configuration.set(path + ".cuboid.location1", LocationUtil.serialize(getLowerCorner()));
        configuration.set(path + ".cuboid.location2", LocationUtil.serialize(getUpperCorner()));
        configuration.set(path + ".kits", getKits());

        if (!duplicates.isEmpty()) {
            System.out.println("Duplicates not empty");
            int i = 0;

            for (Arena duplicate : duplicates) {
                i++;

                configuration.set(path + ".duplicates." + i + ".cuboid.location1", LocationUtil.serialize(duplicate.getLowerCorner()));
                configuration.set(path + ".duplicates." + i + ".cuboid.location2", LocationUtil.serialize(duplicate.getUpperCorner()));
                configuration.set(path + ".duplicates." + i + ".spawnA", LocationUtil.serialize(duplicate.getSpawnA()));
                configuration.set(path + ".duplicates." + i + ".spawnB", LocationUtil.serialize(duplicate.getSpawnB()));
            }
        } else {
            System.out.println("Duplicates empty");
        }

        try {
            configuration.save(Practice.getInstance().getArenasConfig().getFile());
        } catch (IOException ignored) {
        }
    }

    @Override
    public void delete() {
        super.delete();

        FileConfiguration configuration = Practice.getInstance().getArenasConfig().getConfiguration();
        configuration.set("arenas." + getName(), null);

        try {
            configuration.save(Practice.getInstance().getArenasConfig().getFile());
        } catch (IOException ignored) {
        }
    }

}
