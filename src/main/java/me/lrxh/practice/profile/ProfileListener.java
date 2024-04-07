package me.lrxh.practice.profile;

import me.jumper251.replay.filesystem.saving.ReplaySaver;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.MatchState;
import me.lrxh.practice.profile.hotbar.HotbarItem;
import me.lrxh.practice.profile.visibility.VisibilityLogic;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.PlaceholderUtil;
import me.lrxh.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSpawnTeleportEvent(SpawnTeleportEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (!profile.isBusy() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            Practice.getInstance().getHotbar().giveHotbarItems(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].substring(1);
        if (command.equalsIgnoreCase("msg")) return;
        if (command.equalsIgnoreCase("reply")) return;
        if (command.equalsIgnoreCase("r")) return;
        if (command.equalsIgnoreCase("elo")) return;

        if (Practice.getInstance().isReplay()) {
            if (PlayerUtil.inReplay(player)) {
                player.sendMessage(CC.RED + "You cannot run commands while in replay mode.");
                event.setCancelled(true);
            }
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
                    PlayerUtil.teleportToSpawn((Player) event.getEntity());
                }

                Match match = profile.getMatch();
                if (match != null && match.getState().equals(MatchState.ENDING_MATCH)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void soilChangePlayer(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.SOIL)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();

            HotbarItem hotbarItem = Practice.getInstance().getHotbar().fromItemStack(event.getItem());

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

        PlayerUtil.teleportToSpawn(player);
        player.setPlayerTime(profile.getOptions().time().getTime(), false);

        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            VisibilityLogic.handle(player, otherPlayer);
            VisibilityLogic.handle(otherPlayer, player);
        }
        for (String line : Practice.getInstance().getMessagesConfig().getStringList("JOIN_MESSAGE")) {
            ArrayList<String> list = new ArrayList<>();
            list.add(CC.translate(line));
            player.sendMessage(PlaceholderUtil.format(list, player).toString().replace("[", "").replace("]", ""));
        }

        if (player.hasPermission("practice.donor.fly")) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        PlayerUtil.reset(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                Practice.getInstance().getHotbar().giveHotbarItems(event.getPlayer());
            }
        }.runTaskLater(Practice.getInstance(), 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());
        if (Practice.getInstance().isReplay()) {
                if (ReplaySaver.exists(profile.getUuid().toString())) {
                    ReplaySaver.delete(profile.getUuid().toString());
            }
        }

        if (!profile.getFollowers().isEmpty()) {
            for (UUID playerUUID : profile.getFollowers()) {
                Bukkit.getPlayer(playerUUID).sendMessage(Locale.FOLLOW_END.format(Bukkit.getPlayer(playerUUID), event.getPlayer().getName()));
                Bukkit.getPlayer(playerUUID).sendMessage(Locale.FOLLOWED_LEFT.format(Bukkit.getPlayer(playerUUID), event.getPlayer().getName()));
                Profile.getByUuid(playerUUID).getFollowing().remove(event.getPlayer().getUniqueId());
            }
        }

        if (!profile.getFollowing().isEmpty()) {
            List<UUID> followingCopy = new ArrayList<>(profile.getFollowing());

            for (UUID playerUUID : followingCopy) {
                Profile followerProfile = Profile.getByUuid(playerUUID);
                followerProfile.getFollowers().remove(event.getPlayer().getUniqueId());

                profile.getFollowing().remove(playerUUID);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                profile.save();
            }
        }.runTaskAsynchronously(Practice.getInstance());

        if (profile.getMatch() != null) {
            if (profile.getMatch().getState().equals(MatchState.PLAYING_ROUND)
                    || profile.getMatch().getState().equals(MatchState.ENDING_MATCH)
                    || profile.getMatch().getState().equals(MatchState.STARTING_ROUND)) {
                profile.getMatch().sendDeathMessage(event.getPlayer(), null, false);
            }

            profile.getMatch().end();
        }
        if (profile.getState().equals(ProfileState.QUEUEING)) {
            profile.getQueueProfile().getQueue().removeQueue();
        }
        if (profile.getQueueProfile() != null) {
            Practice.getInstance().getCache().getPlayers().remove(profile.getQueueProfile());
        }

        if (profile.getRematchData() != null) {
            profile.getRematchData().validate();
        }

        Profile.getProfiles().remove(event.getPlayer().getUniqueId());
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
