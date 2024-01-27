package me.funky.praxi.event.game;

import me.funky.praxi.event.impl.sumo.SumoEvent;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.profile.hotbar.Hotbar;
import me.funky.praxi.profile.hotbar.HotbarItem;

import java.util.regex.Matcher;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class EventGameListener implements Listener {

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getState() == ProfileState.EVENT) {
            EventGame.getActiveGame().getGameLogic().onLeave(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getState() == ProfileState.EVENT && EventGame.getActiveGame() != null) {
            if (EventGame.getActiveGame().getGameState() == EventGameState.PLAYING_ROUND) {
                EventGame.getActiveGame().getGameLogic().onMove(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = null;

            if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();

                if (projectile.getShooter() instanceof Player) {
                    attacker = (Player) projectile.getShooter();
                }
            }

            if (attacker != null) {
                Profile victimProfile = Profile.getByUuid(victim.getUniqueId());

                if (victimProfile.getState() == ProfileState.EVENT) {
                    Profile attackerProfile = Profile.getByUuid(attacker.getUniqueId());

                    if (attackerProfile.getState() == ProfileState.EVENT) {
                        if (!EventGame.getActiveGame().getGameLogic().isPlaying(victim) ||
                                !EventGame.getActiveGame().getGameLogic().isPlaying(attacker) ||
                                EventGame.getActiveGame().getGameState() != EventGameState.PLAYING_ROUND) {
                            event.setCancelled(true);
                        } else {
                            if (EventGame.getActiveGame().getEvent() instanceof SumoEvent) {
                                event.setDamage(0);
                            }
                        }
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getState() == ProfileState.EVENT) {
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (EventGame.getActiveGame().getGameState() == EventGameState.PLAYING_ROUND &&
                            EventGame.getActiveGame().getGameLogic().isPlaying(player)) {
                        EventGame.getActiveGame().getGameLogic().onDeath(player, null);
                    }
                } else if (EventGame.getActiveGame().getEvent() instanceof SumoEvent) {
                    event.setDamage(0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getItem() != null &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
            Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

            if (profile.getState() == ProfileState.EVENT && EventGame.getActiveGame() != null) {
                ItemStack itemStack = event.getItem();
                ItemStack voteItem = Hotbar.getItems().get(HotbarItem.MAP_SELECTION);

                if (itemStack.getType() == voteItem.getType() &&
                        itemStack.getDurability() == voteItem.getDurability()) {
                    Matcher matcher = HotbarItem.MAP_SELECTION
                            .getPattern().matcher(itemStack.getItemMeta().getDisplayName());

                    if (matcher.find()) {
                        String mapName = matcher.group(2);

                        event.setCancelled(true);
                        event.getPlayer().chat("/event map vote " + mapName);
                    }
                }
            }
        }
    }

}
