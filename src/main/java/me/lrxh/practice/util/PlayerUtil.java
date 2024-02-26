package me.lrxh.practice.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.lrxh.practice.Practice;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PlayerUtil {

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

    public static void animateDeath(Player player) {
        try {
            final int radius = Bukkit.getServer().getViewDistance() * 16;

            PacketContainer namedEntitySpawnPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
            namedEntitySpawnPacket.getIntegers().write(0, player.getEntityId()).write(1, -1);

            PacketContainer entityStatusPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_STATUS);
            entityStatusPacket.getIntegers().write(0, player.getEntityId()).write(1, (int) (byte) 3);

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof Player)) {
                    continue;
                }

                Player watcher = (Player) entity;

                if (!watcher.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }

                ProtocolLibrary.getProtocolManager().sendServerPacket(watcher, namedEntitySpawnPacket);
                ProtocolLibrary.getProtocolManager().sendServerPacket(watcher, entityStatusPacket);
            }
        } catch (Exception ignored) {
        }
    }
}
