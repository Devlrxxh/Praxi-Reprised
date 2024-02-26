package me.lrxh.practice.essentials;

import me.lrxh.practice.Practice;
import me.lrxh.practice.essentials.event.SpawnTeleportEvent;
import me.lrxh.practice.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.IOException;

public class Essentials {

    private final Practice practice;
    private Location spawn;

    public Essentials(Practice practice) {
        this.practice = practice;
        this.spawn = LocationUtil.deserialize(practice.getMainConfig().getStringOrDefault("ESSENTIAL.SPAWN_LOCATION", null));
    }

    public void setSpawn(Location location) {
        spawn = location;

        if (spawn == null) {
            practice.getMainConfig().getConfiguration().set("ESSENTIAL.SPAWN_LOCATION", null);
        } else {
            practice.getMainConfig().getConfiguration().set("ESSENTIAL.SPAWN_LOCATION", LocationUtil.serialize(this.spawn));
        }

        try {
            practice.getMainConfig().getConfiguration().save(practice.getMainConfig().getFile());
        } catch (IOException ignored) {
        }
    }

    public void teleportToSpawn(Player player) {
        Location location = spawn == null ? practice.getServer().getWorlds().get(0).getSpawnLocation() : spawn;

        SpawnTeleportEvent event = new SpawnTeleportEvent(player, location);
        event.call();

        if (!event.isCancelled() && event.getLocation() != null) {
            player.teleport(event.getLocation());
        }
    }

    public void clearEntities(World world) {
        for (Entity entity : world.getEntities()) {
            if (entity.getType() == EntityType.PLAYER) {
                continue;
            }

            entity.remove();
        }
    }
}
