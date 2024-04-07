package me.lrxh.practice.util;

import lombok.experimental.UtilityClass;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.SpawnTeleportEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.github.paperspigot.Title;

import java.lang.reflect.Field;
import java.util.*;

@UtilityClass
public class PlayerUtil {
    private Field STATUS_PACKET_ID_FIELD;
    private Field STATUS_PACKET_STATUS_FIELD;
    private Field SPAWN_PACKET_ID_FIELD;

    static {
        try {
            STATUS_PACKET_ID_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("a");
            STATUS_PACKET_ID_FIELD.setAccessible(true);

            STATUS_PACKET_STATUS_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("b");
            STATUS_PACKET_STATUS_FIELD.setAccessible(true);

            SPAWN_PACKET_ID_FIELD = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("a");
            SPAWN_PACKET_ID_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void setLastAttacker(Player victim, Player attacker) {
        if (attacker == null) {
            victim.setMetadata("lastAttacker", new FixedMetadataValue(Practice.getInstance(), null));
        } else {
            victim.setMetadata("lastAttacker", new FixedMetadataValue(Practice.getInstance(), attacker.getUniqueId()));
        }
    }

    public void setInParty(Player victim, Boolean value) {
        victim.setMetadata("inParty", new FixedMetadataValue(Practice.getInstance(), value));
    }

    public Boolean inReplay(Player player) {
        if (player.hasMetadata("inReplay")) {
            return player.getMetadata("inReplay").get(0).value().equals(true);
        }
        return false;
    }


    public static void setImmune(Player player, int ticks) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, ticks, 250));
    }

    public Player getLastAttacker(Player victim) {
        if (victim.hasMetadata("lastAttacker")) {
            return Bukkit.getPlayer((UUID) victim.getMetadata("lastAttacker").get(0).value());
        } else {
            return null;
        }
    }

    public void sendMessage(Player player, ChatComponentBuilder[]... chatComponentBuilders) {
        List<BaseComponent[]> components = new ArrayList<>();
        for (ChatComponentBuilder[] builderArray : chatComponentBuilders) {
            for (ChatComponentBuilder chatComponentBuilder : builderArray) {
                components.add(chatComponentBuilder.create());
            }
        }
        if (player != null) {
            BaseComponent[] concatenatedComponents = concatenateComponents(components);
            player.sendMessage(concatenatedComponents);
        }
    }


    private BaseComponent[] concatenateComponents(List<BaseComponent[]> components) {
        int totalLength = 0;
        for (BaseComponent[] component : components) {
            totalLength += component.length;
        }
        BaseComponent[] concatenatedComponents = new BaseComponent[totalLength];
        int currentIndex = 0;
        for (BaseComponent[] component : components) {
            System.arraycopy(component, 0, concatenatedComponents, currentIndex, component.length);
            currentIndex += component.length;
        }
        return concatenatedComponents;
    }

    public static void sendTitle(Player player, String header, String footer, int duration) {
        player.sendTitle(new Title(CC.translate(header), CC.translate(footer), 1, duration, 10));
    }

    public ItemStack getPlayerHead(UUID playerUUID) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwner(Bukkit.getPlayer(playerUUID).getName());
        head.setItemMeta(skullMeta);
        return head;
    }

    public void reset(Player player) {

        if (!player.hasMetadata("frozen")) {
            player.setWalkSpeed(0.2F);
            player.setFlySpeed(0.1F);
        }

        player.setHealth(20.0D);
        player.setSaturation(20.0F);
        player.setFallDistance(0.0F);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setMaximumNoDamageTicks(20);
        player.setExp(0.0F);
        player.setLevel(0);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setContents(new ItemStack[36]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.getInventory().setHeldItemSlot(0);
        player.updateInventory();
    }

    public void applyFireballKnockback(Location location, List<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) continue;
            Player player = (Player) entity;
            if (player.getGameMode() != GameMode.SURVIVAL) continue;
            double vertical = 2 / 2.0;
            double reference = 3 / 2.0;

            double magnitude = Math.max(-15.0, Math.min(15.0, -1.0 * reference));

            Vector velocity = location.toVector().subtract(player.getLocation().toVector()).normalize();
            velocity.multiply(magnitude);
            velocity.setY(vertical);

            player.setVelocity(velocity);
        }
    }

    public void doVelocityChange(Player player) {
        player.setVelocity(player.getVelocity().add(new Vector(0, 0.25, 0)));
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setVelocity(player.getVelocity().add(new Vector(0, 0.15, 0)));
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void denyMovement(Player player) {
        if (player == null) {
            return;
        }
        player.setFlying(false);
        player.setWalkSpeed(0.0F);
        player.setFoodLevel(0);
        player.setSprinting(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200));
    }

    public void allowMovement(Player player) {
        if (player == null) {
            return;
        }
        player.setFlying(false);
        player.setWalkSpeed(0.2F);
        player.setFoodLevel(20);
        player.setSprinting(true);
        player.removePotionEffect(PotionEffectType.JUMP);
    }


    public void teleportToSpawn(Player player) {
        Location location = Practice.getInstance().getCache().getSpawn() == null ? Practice.getInstance().getServer().getWorlds().get(0).getSpawnLocation() : Practice.getInstance().getCache().getSpawn();

        SpawnTeleportEvent event = new SpawnTeleportEvent(player, location);
        event.call();

        if (!event.isCancelled() && event.getLocation() != null) {
            player.teleport(event.getLocation());
        }
    }

    public void animateDeath(Player player) {
        int entityId = EntityUtils.getFakeEntityId();
        PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle());
        PacketPlayOutEntityStatus statusPacket = new PacketPlayOutEntityStatus();

        try {
            SPAWN_PACKET_ID_FIELD.set(spawnPacket, entityId);
            STATUS_PACKET_ID_FIELD.set(statusPacket, entityId);
            STATUS_PACKET_STATUS_FIELD.set(statusPacket, (byte) 3);
            int radius = MinecraftServer.getServer().getPlayerList().d();
            Set<Player> sentTo = new HashSet<>();
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof Player) {
                    Player watcher = (Player) entity;
                    if (!watcher.getUniqueId().equals(player.getUniqueId())) {
                        ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(spawnPacket);
                        ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(statusPacket);
                        sentTo.add(watcher);
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(Practice.getInstance(), () -> {
                for (Player watcher : sentTo) {
                    ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityId));
                }
            }, 10L);
        } catch (IllegalAccessException ignored) {
        }
    }
}
