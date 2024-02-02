package me.funky.praxi.arena;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.cuboid.Cuboid;
import me.funky.praxi.arena.impl.SharedArena;
import me.funky.praxi.arena.impl.StandaloneArena;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.util.LocationUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Arena extends Cuboid {

    @Getter
    private static final List<Arena> arenas = new ArrayList<>();

    protected String name;
    protected Location spawnA;
    protected Location spawnB;
    protected boolean active;
    private List<String> kits = new ArrayList<>();

    public Arena(String name, Location location1, Location location2) {
        super(location1, location2);

        this.name = name;
    }

    public static void init() {
        FileConfiguration configuration = Praxi.getInstance().getArenasConfig().getConfiguration();

        if (configuration.contains("arenas")) {
            for (String arenaName : configuration.getConfigurationSection("arenas").getKeys(false)) {
                String path = "arenas." + arenaName;

                ArenaType arenaType = ArenaType.valueOf(configuration.getString(path + ".type"));
                Location location1 = LocationUtil.deserialize(configuration.getString(path + ".cuboid.location1"));
                Location location2 = LocationUtil.deserialize(configuration.getString(path + ".cuboid.location2"));

                Arena arena;

                if (arenaType == ArenaType.STANDALONE) {
                    arena = new StandaloneArena(arenaName, location1, location2);
                } else if (arenaType == ArenaType.SHARED) {
                    arena = new SharedArena(arenaName, location1, location2);
                } else {
                    continue;
                }

                if (configuration.contains(path + ".spawnA")) {
                    arena.setSpawnA(LocationUtil.deserialize(configuration.getString(path + ".spawnA")));
                }

                if (configuration.contains(path + ".spawnB")) {
                    arena.setSpawnB(LocationUtil.deserialize(configuration.getString(path + ".spawnB")));
                }

                if (configuration.contains(path + ".kits")) {
                    for (String kitName : configuration.getStringList(path + ".kits")) {
                        arena.getKits().add(kitName);
                    }
                }

                if (arena instanceof StandaloneArena && configuration.contains(path + ".duplicates")) {
                    for (String duplicateId : configuration.getConfigurationSection(path + ".duplicates").getKeys(false)) {
                        location1 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".cuboid.location1"));
                        location2 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".cuboid.location2"));
                        Location spawn1 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".spawnA"));
                        Location spawn2 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".spawnB"));

                        Arena duplicate = new Arena(arenaName, location1, location2);

                        duplicate.setSpawnA(spawn1);
                        duplicate.setSpawnB(spawn2);
                        duplicate.setKits(arena.getKits());

                        ((StandaloneArena) arena).getDuplicates().add(duplicate);

                        Arena.getArenas().add(duplicate);
                    }
                }

                Arena.getArenas().add(arena);
            }
        }

        Praxi.getInstance().getLogger().info("Loaded " + Arena.getArenas().size() + " arenas");
    }

    public static Arena getByName(String name) {
        for (Arena arena : arenas) {
            if (arena.getType() != ArenaType.DUPLICATE && arena.getName() != null &&
                    arena.getName().equalsIgnoreCase(name)) {
                return arena;
            }
        }

        return null;
    }


    public static Arena getRandomArena(Kit kit) {
        List<Arena> _arenas = new ArrayList<>();

        for (Arena arena : arenas) {
            if (!arena.isSetup()) {
                continue;
            }

            if (!arena.getKits().contains(kit.getName())) {
                continue;
            }

            if (kit.getGameRules().isBuild() && !arena.isActive() && (arena.getType() == ArenaType.STANDALONE ||
                    arena.getType() == ArenaType.DUPLICATE)) {
                _arenas.add(arena);
            } else if (!kit.getGameRules().isBuild() && arena.getType() == ArenaType.SHARED) {
                _arenas.add(arena);
            }
        }

        if (_arenas.isEmpty()) {
            return null;
        }

        return _arenas.get(ThreadLocalRandom.current().nextInt(_arenas.size()));
    }

    public ArenaType getType() {
        return ArenaType.DUPLICATE;
    }

    public boolean isSetup() {
        return getLowerCorner() != null && getUpperCorner() != null && spawnA != null && spawnB != null;
    }

    public int getMaxBuildHeight() {
        int highest = (int) (Math.max(spawnA.getY(), spawnB.getY()));
        return highest + 5;
    }

    public void unloadArena() {
        for (Chunk chunk : getChunks()) {
            chunk.getWorld().unloadChunk(chunk);
        }
    }

    public void loadArena() {
        for (Chunk chunk : getChunks()) {
            chunk.getWorld().loadChunk(chunk);
        }
    }

    public Location getSpawnA() {
        if (spawnA == null) {
            return null;
        }

        return spawnA.clone();
    }

    public Location getSpawnB() {
        if (spawnB == null) {
            return null;
        }

        return spawnB.clone();
    }

    public void setActive(boolean active) {
        if (getType() != ArenaType.SHARED) {
            this.active = active;
        }
    }

    public void save() {

    }

    public void delete() {
        arenas.remove(this);
    }

}
