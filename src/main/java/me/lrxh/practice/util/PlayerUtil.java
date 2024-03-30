package me.lrxh.practice.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.SpawnTeleportEvent;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

import java.lang.reflect.Field;
import java.util.UUID;

public class PlayerUtil {
    private static Field STATUS_PACKET_ID_FIELD;
    private static Field STATUS_PACKET_STATUS_FIELD;
    private static Field SPAWN_PACKET_ID_FIELD;

    public static void setLastAttacker(Player victim, Player attacker) {
        victim.setMetadata("lastAttacker", new FixedMetadataValue(Practice.getInstance(), attacker.getUniqueId()));
    }

    public static Player getLastAttacker(Player victim) {
        if (victim.hasMetadata("lastAttacker")) {
            return Bukkit.getPlayer((UUID) victim.getMetadata("lastAttacker").get(0).value());
        } else {
            return null;
        }
    }

    public static void reset(Player player) {

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

    public static void denyMovement(Player player) {
        player.setFlying(false);
        player.setWalkSpeed(0.0F);
        player.setFoodLevel(0);
        player.setSprinting(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200));
    }

    public static void allowMovement(Player player) {
        player.setFlying(false);
        player.setWalkSpeed(0.2F);
        player.setFoodLevel(20);
        player.setSprinting(true);
        player.removePotionEffect(PotionEffectType.JUMP);
    }

    public static Block getTargetBlock(Player player, int distance) {
        BlockIterator iterator = new BlockIterator(player, distance);
        Block block = null;

        while (iterator.hasNext()) {
            block = iterator.next();
            if (block.getType().isSolid()) {
                break;
            }
        }

        return block;
    }

    public static void teleportToSpawn(Player player) {
        Location location = Practice.getInstance().getCache().getSpawn() == null ? Practice.getInstance().getServer().getWorlds().get(0).getSpawnLocation() : Practice.getInstance().getCache().getSpawn();

        SpawnTeleportEvent event = new SpawnTeleportEvent(player, location);
        event.call();

        if (!event.isCancelled() && event.getLocation() != null) {
            player.teleport(event.getLocation());
        }
    }

    public static void animateDeath(Player player) {

        try {
            if (STATUS_PACKET_ID_FIELD == null) {
                STATUS_PACKET_ID_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("a");
                STATUS_PACKET_ID_FIELD.setAccessible(true);
            }

            if (STATUS_PACKET_STATUS_FIELD == null) {
                STATUS_PACKET_STATUS_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("b");
                STATUS_PACKET_STATUS_FIELD.setAccessible(true);
            }

            if (SPAWN_PACKET_ID_FIELD == null) {
                SPAWN_PACKET_ID_FIELD = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("a");
                SPAWN_PACKET_ID_FIELD.setAccessible(true);
            }

            SPAWN_PACKET_ID_FIELD.set(new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle()), -1);
            STATUS_PACKET_ID_FIELD.set(new PacketPlayOutEntityStatus(), -1);
            STATUS_PACKET_STATUS_FIELD.set(new PacketPlayOutEntityStatus(), (byte) 3);

            final int radius = MinecraftServer.getServer().getPlayerList().d();

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof Player)) {
                    Player watcher = (Player) entity;

                    if (!watcher.getUniqueId().equals(player.getUniqueId())) {
                        break;
                    }

                    ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle()));
                    ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityStatus());
                }
            }
            } catch(Exception ignored){
            }
        }
    }
