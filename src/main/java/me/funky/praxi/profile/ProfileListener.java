package me.funky.praxi.profile;

import me.funky.praxi.Praxi;
import me.funky.praxi.essentials.event.SpawnTeleportEvent;
import me.funky.praxi.profile.hotbar.Hotbar;
import me.funky.praxi.profile.hotbar.HotbarItem;
import me.funky.praxi.profile.meta.option.button.AllowSpectatorsOptionButton;
import me.funky.praxi.profile.meta.option.button.DuelRequestsOptionButton;
import me.funky.praxi.profile.meta.option.button.ShowScoreboardOptionButton;
import me.funky.praxi.profile.option.OptionsOpenedEvent;
import me.funky.praxi.profile.visibility.VisibilityLogic;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class ProfileListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSpawnTeleportEvent(SpawnTeleportEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (!profile.isBusy() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            Hotbar.giveHotbarItems(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getState() != ProfileState.FIGHTING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getState() != ProfileState.FIGHTING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() && event.getRightClicked() instanceof Player
                && Profile.getByUuid(player.getUniqueId()).getState().equals(ProfileState.LOBBY)) {
            Player clickedPlayer = (Player) event.getRightClicked();
            player.chat("/duel " + clickedPlayer.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getState() == ProfileState.LOBBY) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

            if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.QUEUEING) {
                event.setCancelled(true);

                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    Praxi.getInstance().getEssentials().teleportToSpawn((Player) event.getEntity());
                }
            }
        }
    }

    @EventHandler
    public void onOptionsOpenedEvent(OptionsOpenedEvent event) {
        event.getButtons().add(new ShowScoreboardOptionButton());
        event.getButtons().add(new AllowSpectatorsOptionButton());
        event.getButtons().add(new DuelRequestsOptionButton());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();

            HotbarItem hotbarItem = Hotbar.fromItemStack(event.getItem());

            if (hotbarItem != null) {
                if (hotbarItem.getCommand() != null) {
                    event.setCancelled(true);
                    player.chat("/" + hotbarItem.getCommand());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        Profile profile = new Profile(player.getUniqueId());

        try {
            profile.load();
        } catch (Exception e) {
            event.getPlayer().kickPlayer(ChatColor.RED + "Failed to load your profile. Try again later.");
            return;
        }
        Profile.getProfiles().put(player.getUniqueId(), profile);

        Praxi.getInstance().getEssentials().teleportToSpawn(player);

        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            VisibilityLogic.handle(player, otherPlayer);
            VisibilityLogic.handle(otherPlayer, player);
        }
        for (String line : Praxi.getInstance().getMainConfig().getStringList("JOIN_MESSAGE")) {
            ArrayList<String> list = new ArrayList<>();
            list.add(CC.translate(line));
            player.sendMessage(PlaceholderUtil.format(list, player).toString().replace("[", "").replace("]", ""));
        }

        if (player.hasPermission("praxi.donor.fly")) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.updateInventory();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Hotbar.giveHotbarItems(event.getPlayer());
            }
        }.runTaskLater(Praxi.getInstance(), 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                profile.save();
                Profile.getProfiles().remove(event.getPlayer().getUniqueId());
            }
        }.runTaskAsynchronously(Praxi.getInstance());

        if (profile.getState().equals(ProfileState.FIGHTING)) {
            profile.getMatch().end();

        }
        if (profile.getState().equals(ProfileState.QUEUEING)) {
            profile.getQueueProfile().getQueue().getKit().removeQueue();
        }
        if (profile.getQueueProfile() != null) {
            Praxi.getInstance().getCache().getPlayers().remove(profile.getQueueProfile());
        }

        if (profile.getRematchData() != null) {
            profile.getRematchData().validate();
        }
    }

    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent event) {
        if (event.getReason() != null) {
            if (event.getReason().contains("Flying is not enabled")) {
                event.setCancelled(true);
            }
        }
    }

}
