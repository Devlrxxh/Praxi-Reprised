package me.funky.praxi.essentials;

import me.funky.praxi.Practice;
import me.funky.praxi.essentials.event.SpawnTeleportEvent;
import me.funky.praxi.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
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
        } catch (IOException e) {
            e.printStackTrace();
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

    public int clearEntities(World world) {
        int removed = 0;

        for (Entity entity : world.getEntities()) {
            if (entity.getType() == EntityType.PLAYER) {
                continue;
            }

            removed++;
            entity.remove();
        }

        return removed;
    }

    public int clearEntities(World world, EntityType... excluded) {
        int removed = 0;

        entityLoop:
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Item) {
                removed++;
                entity.remove();
                continue entityLoop;
            }

            for (EntityType type : excluded) {
                if (entity.getType() == EntityType.PLAYER) {
                    continue entityLoop;
                }

                if (entity.getType() == type) {
                    continue entityLoop;
                }
            }

            removed++;
            entity.remove();
        }

        return removed;
    }

}
