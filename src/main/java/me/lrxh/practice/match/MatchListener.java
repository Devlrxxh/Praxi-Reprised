package me.lrxh.practice.match;

import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.kit.KitLoadout;
import me.lrxh.practice.match.menu.ViewInventoryMenu;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.profile.hotbar.HotbarItem;
import me.lrxh.practice.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;

public class MatchListener implements Listener {


    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        Match match = profile.getMatch();


        if (profile.getMatch() != null) {
            if (profile.getMatch().getKit().getGameRules().isSumo() ||
                    profile.getMatch().getKit().getGameRules().isSpleef()) {
                Location playerLocation = event.getPlayer().getLocation();
                Block block = playerLocation.getBlock();

                if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                    match.onDeath(event.getPlayer());
                }
            }
            if (match.getKit().getGameRules().isBedwars()) {
                Player player = event.getPlayer();

                boolean bedGone = match.getParticipantA().containsPlayer(player.getUniqueId()) ? match.bedBBroken : match.bedABroken;

                if (profile.getMatch().kit.getGameRules().isBedwars()) {
                    if (!(player.getLocation().getY() >= match.getArena().getDeathZone()) && !match.getGamePlayer(player).isRespawned()) {
                        if (!bedGone) {

                            if (PlayerUtil.getLastAttacker(player) != null) {
                                PlayerUtil.getLastAttacker(player).playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                            }
                            match.respawn(player.getUniqueId());
                        } else {
                            profile.getMatch().onDeath(player);
                        }
                    }
                }
            }
        }
    }


//        @EventHandler
//    public void onExplosionKB(EntityExplodeEvent event) {
//        if (!(event.getEntity() instanceof Player)) return;
//        if (!(event.getEntityType() == EntityType.FIREBALL)) return;
//
//        PlayerUtil.applyFireballKnockback(event.getEntity().getLocation(), Collections.singletonList(event.getEntity()));
//    }
//
//    @EventHandler
//    public void onFireballLaunch(PlayerInteractEvent event) {
//        if (event.getAction().name().contains("RIGHT") && event.hasItem() &&
//                event.getItem().getType().equals(Material.FIREBALL)) {
//            Player player = event.getPlayer();
//            Match match = Profile.getByUuid(player.getUniqueId()).getMatch();
//            LargeFireball fireball = player.launchProjectile(LargeFireball.class);
//            fireball.setMetadata("Bolt", new FixedMetadataValue(Practice.getInstance(), match.getMatchId().toString()));
//            fireball.setYield(3);
//            fireball.setBounce(false);
//
//            // Deletes the fireball when player dies, disconnects or match ends.
//            //FireballExpireTask task = new FireballExpireTask(match, match.getMatchPlayer(player), fireball);
//            //task.runTaskTimerAsynchronously(Bolt.getInstance(), 20L, 20L);
//
//            Vector direction = player.getLocation().getDirection();
//            fireball.setVelocity(direction);
//        }
//    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getMatch() != null) {
            Match match = profile.getMatch();

            if (match.getKit().getGameRules().isBuild() && match.getState() == MatchState.PLAYING_ROUND) {
                if (match.getKit().getGameRules().isSpleef()) {
                    event.setCancelled(true);
                    return;
                }

                Arena arena = match.getArena();
                Location blockLocation = event.getBlockPlaced().getLocation();
                int x = (int) event.getBlockPlaced().getLocation().getX();
                int y = (int) event.getBlockPlaced().getLocation().getY();
                int z = (int) event.getBlockPlaced().getLocation().getZ();
                Location newBlockLocation = new Location(arena.getWorld(), x, y, z);
                if (newBlockLocation.equals(new Location(arena.getSpawnA().getWorld(), (int) arena.getSpawnA().getX(), (int) arena.getSpawnA().getY(), (int) arena.getSpawnA().getZ()))
                        || newBlockLocation.equals(new Location(arena.getSpawnB().getWorld(), (int) arena.getSpawnB().getX(), (int) arena.getSpawnB().getY(), (int) arena.getSpawnB().getZ()))
                        || newBlockLocation.equals(new Location(arena.getSpawnA().getWorld(), (int) arena.getSpawnA().getX(), (int) arena.getSpawnA().getY() + 1, (int) arena.getSpawnA().getZ()))
                        || newBlockLocation.equals(new Location(arena.getSpawnB().getWorld(), (int) arena.getSpawnB().getX(), (int) arena.getSpawnB().getY() + 1, (int) arena.getSpawnB().getZ()))) {

                    event.getPlayer().sendMessage(CC.translate("&cYou cannot place block blocks here!"));
                    event.setCancelled(true);
                    return;
                }

                if (y > arena.getMaxBuildHeight()) {
                    event.getPlayer().sendMessage(CC.RED + "You have reached the maximum build height.");
                    event.setCancelled(true);
                    return;
                }

                if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() &&
                        z >= arena.getZ1() && z <= arena.getZ2()) {
                    match.getPlacedBlocks().add(blockLocation);
                } else {
                    event.getPlayer().sendMessage(CC.RED + "You cannot build outside of the arena!");
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
            if (match.getGamePlayer(event.getPlayer()).isRespawned()) {
                event.setCancelled(true);
            }
        } else {
            if (!event.getPlayer().isOp() || event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFallDamageEvent(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getMatch() != null) {
            Match match = profile.getMatch();
            if (event.getBlock().getType() == Material.BED_BLOCK || event.getBlock().getType() == Material.BED) {
                Player player = event.getPlayer();

                GameParticipant<MatchGamePlayer> participantA = match.getParticipantA();

                boolean aTeam = match.getParticipantA().containsPlayer(player.getUniqueId());

                Location spawn = aTeam ? match.getArena().getSpawnA() : match.getArena().getSpawnB();

                Location bed = event.getBlock().getLocation();
                if (bed.distanceSquared(spawn) > bed.distanceSquared(participantA != null && participantA.containsPlayer(match.getOpponent(player.getUniqueId()).getUniqueId()) ?
                        match.getArena().getSpawnA() : match.getArena().getSpawnB())) {

                    if (aTeam) {
                        match.setBedABroken(true);
                    } else {
                        match.setBedBBroken(true);
                    }

                } else if (bed.distanceSquared(spawn) < bed.distanceSquared(match.getOpponent(player.getUniqueId()).getLocation())) {
                    player.sendMessage(CC.translate("&cYou cannot break your own bed!"));
                    event.setCancelled(true);
                    return;
                }

                if (!aTeam) {
                    match.sendTitleA("&cBED DESTROYED!", "&fYou will no longer respawn!", 40);
                } else {
                    match.sendTitleB("&cBED DESTROYED!", "&fYou will no longer respawn!", 40);
                }

                match.sendSound(Sound.ORB_PICKUP, 1.0F, 1.0F);
                match.sendSound(Sound.WITHER_DEATH, 1.0F, 1.0F);
                match.broadcast(" ");
                match.broadcast(Locale.MATCH_BED_BROKEN.format(player, aTeam ? CC.translate("&9Blue") : CC.translate("&cRed"),
                        aTeam ? CC.translate("&c" + player.getName()) : CC.translate("&9" + player.getName())));
                match.broadcast(" ");
            }

            if (match.getKit().getGameRules().isBuild() && match.getState() == MatchState.PLAYING_ROUND) {
                if (match.getKit().getGameRules().isSpleef()) {
                    if (event.getBlock().getType() == Material.SNOW_BLOCK ||
                            event.getBlock().getType() == Material.SNOW) {
                        match.getChangedBlocks().add(event.getBlock().getState());

                        event.getBlock().setType(Material.AIR);
                        event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
                        event.getPlayer().updateInventory();
                    } else {
                        event.setCancelled(true);
                    }
                } else if (!match.getPlacedBlocks().remove(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
            if (match.kit.getGameRules().isBedwars() && (event.getBlock().getType().equals(Material.BED_BLOCK) || event.getBlock().getType().equals(Material.BED)) || event.getBlock().getType().equals(Material.ENDER_STONE) || (event.getBlock().getType().equals(Material.WOOD) && event.getBlock().getData() == 0)) {
                event.setCancelled(false);
            }
            if (match.getGamePlayer(event.getPlayer()).isRespawned()) {
                event.setCancelled(true);
            }
        } else {
            if (!event.getPlayer().isOp() || event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getMatch() != null) {
            Match match = profile.getMatch();

            if (match.getKit().getGameRules().isBuild() && match.getState() == MatchState.PLAYING_ROUND) {
                Arena arena = match.getArena();
                Block block = event.getBlockClicked().getRelative(event.getBlockFace());
                int x = (int) block.getLocation().getX();
                int y = (int) block.getLocation().getY();
                int z = (int) block.getLocation().getZ();

                if (y > arena.getMaxBuildHeight()) {
                    event.getPlayer().sendMessage(CC.RED + "You have reached the maximum build height.");
                    event.setCancelled(true);
                    return;
                }

                if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() &&
                        z >= arena.getZ1() && z <= arena.getZ2()) {
                    match.getPlacedBlocks().add(block.getLocation());
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            if (!event.getPlayer().isOp() || event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getMatch() != null) {
            if (profile.getMatch().getGamePlayer(event.getPlayer()).isDead()) {
                event.setCancelled(true);
                return;
            }

            if (event.getItem().getItemStack().getType().name().contains("BOOK")) {
                event.setCancelled(true);
                return;
            }
            if (event.getItem().getItemStack().getType().equals(Material.ENDER_STONE) || (event.getItem().getItemStack().getType().equals(Material.WOOD) || event.getItem().getItemStack().getType().equals(Material.WOOL))) {

                event.setCancelled(false);
                return;
            }

            Iterator<Item> itemIterator = profile.getMatch().getDroppedItems().iterator();

            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();

                if (item.equals(event.getItem())) {
                    itemIterator.remove();
                    return;
                }
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (event.getItemDrop().getItemStack().getType() == Material.BOOK ||
                event.getItemDrop().getItemStack().getType() == Material.ENCHANTED_BOOK) {
            event.setCancelled(true);
            return;
        }

        if (profile.getMatch() != null) {
            if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) {
                event.getItemDrop().remove();
                return;
            }

            if (event.getItemDrop().getItemStack().getType().name().contains("SWORD")) {
                event.setCancelled(true);
                return;
            }

            profile.getMatch().getDroppedItems().add(event.getItemDrop());
        }
    }

    @EventHandler
    public void onPlayerPickUpEvent(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        if (profile.getMatch() != null && profile.getMatch().getState().equals(MatchState.ENDING_MATCH)) {
            event.setCancelled(true);
        }
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setDeathMessage(null);
        event.getDrops().clear();

        Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

        if (profile.getMatch() != null) {

            Match match = profile.getMatch();

            boolean aTeam = match.getParticipantA().containsPlayer(player.getUniqueId());

            boolean bedGone = aTeam ? match.bedBBroken : match.bedABroken;

            if (profile.getMatch().getKit().getGameRules().isBedwars()) {
                event.getDrops().clear();
                if (!bedGone) {

                    if (PlayerUtil.getLastAttacker(player) != null) {
                        PlayerUtil.getLastAttacker(player).playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                    }

                    match.respawn(player.getUniqueId());
                } else {
                    profile.getMatch().onDeath(player);
                }
                return;
            }

            match.onDeath(event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(event.getPlayer().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) event.getEntity().getShooter();
            Profile profile = Profile.getByUuid(shooter.getUniqueId());

            if (profile.getMatch() != null) {
                Match match = profile.getMatch();

                if (match.getState() == MatchState.STARTING_ROUND) {
                    event.setCancelled(true);
                } else if (match.getState() == MatchState.PLAYING_ROUND) {
                    if (event.getEntity() instanceof ThrownPotion) {
                        match.getGamePlayer(shooter).incrementPotionsThrown();
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            if (event.getEntity().getShooter() instanceof Player) {
                Player shooter = (Player) event.getEntity().getShooter();
                Profile shooterData = Profile.getByUuid(shooter.getUniqueId());

                if (shooterData.getState() == ProfileState.FIGHTING) {
                    shooterData.getMatch().getGamePlayer(shooter).handleHit();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPotionSplashEvent(PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player) {
            Player shooter = (Player) event.getPotion().getShooter();
            Profile shooterData = Profile.getByUuid(shooter.getUniqueId());

            if (shooterData.getMatch() != null &&
                    shooterData.getMatch().getState().equals(MatchState.PLAYING_ROUND)) {
                if (event.getIntensity(shooter) <= 0.5D) {
                    shooterData.getMatch().getGamePlayer(shooter).incrementPotionsMissed();
                }

                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (entity instanceof Player) {
                        if (shooterData.getMatch().getGamePlayer((Player) entity) == null) {
                            event.setIntensity(entity, 0);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

                if (profile.getMatch() != null && !profile.getMatch().getKit().getGameRules().isHealthRegeneration()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getMatch() != null) {

                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {

                    Match match = profile.getMatch();
                    GameParticipant<MatchGamePlayer> participantA = match.getParticipant(player);

                    boolean bedGone = participantA != null && participantA.containsPlayer(event.getEntity().getUniqueId()) ? match.bedABroken : match.bedBBroken;
                    if (profile.getMatch().kit.getGameRules().isBedwars() && !bedGone) {
                        Location spawn = participantA != null && participantA.containsPlayer(event.getEntity().getUniqueId()) ?
                                match.getArena().getSpawnA() : match.getArena().getSpawnB();

                        event.getEntity().teleport(spawn);
                    } else {
                        profile.getMatch().onDeath(player);
                    }
                    return;
                }

                if (profile.getMatch().getState() != MatchState.PLAYING_ROUND) {
                    event.setCancelled(true);
                    return;
                }

                if (profile.getMatch().getGamePlayer(player).isDead()) {
                    event.setCancelled(true);
                    return;
                }

                if (profile.getMatch().getKit().getGameRules().isSumo() || profile.getMatch().getKit().getGameRules().isSpleef() || profile.getMatch().getKit().getGameRules().isBoxing()) {
                    event.setDamage(0);
                    player.setHealth(20.0);
                    player.updateInventory();
                }
            }
        }


    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        if (profile.getMatch() == null) return;
        if (profile.getMatch().getKit().getGameRules().isSumo()
                || profile.getMatch().getKit().getGameRules().isBedwars()
                || profile.getMatch().getKit().getGameRules().isSpleef()
                || profile.getMatch().getKit().getGameRules().isBoxing()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntityLow(EntityDamageByEntityEvent event) {
        Player attacker;

        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                attacker = (Player) ((Projectile) event.getDamager()).getShooter();
            } else {
                event.setCancelled(true);
                return;
            }
        } else {
            event.setCancelled(true);
            return;
        }

        if (attacker != null && event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            Profile damagedProfile = Profile.getByUuid(damaged.getUniqueId());
            Profile attackerProfile = Profile.getByUuid(attacker.getUniqueId());

            if (attackerProfile.getState() == ProfileState.SPECTATING || damagedProfile.getState() == ProfileState.SPECTATING) {
                event.setCancelled(true);
                return;
            }

            if (damagedProfile.getState() == ProfileState.FIGHTING && attackerProfile.getState() == ProfileState.FIGHTING) {
                Match match = attackerProfile.getMatch();

                if (!damagedProfile.getMatch().getMatchId().equals(attackerProfile.getMatch().getMatchId())) {
                    event.setCancelled(true);
                    return;
                }

                if (match.getGamePlayer(damaged).isDead()) {
                    event.setCancelled(true);
                    return;
                }

                if (match.getGamePlayer(attacker).isDead()) {
                    event.setCancelled(true);
                    return;
                }

                if (match.isOnSameTeam(damaged, attacker)) {
                    event.setCancelled(true);
                    return;
                }

                attackerProfile.getMatch().getGamePlayer(attacker).handleHit();
                damagedProfile.getMatch().getGamePlayer(damaged).resetCombo();

                if (match.getKit().getGameRules().isBoxing() && match.getState() != MatchState.STARTING_ROUND
                        && match.getState() != MatchState.ENDING_MATCH
                        && attackerProfile.getMatch().getGamePlayer(attacker).getHits() == 100) {
                    match.onDeath(damaged);
                }

                if (event.getDamager() instanceof Arrow) {
                    int range = (int) Math.ceil(event.getEntity().getLocation().distance(attacker.getLocation()));
                    double health = Math.ceil(damaged.getHealth() - event.getFinalDamage()) / 2.0D;

                    attacker.sendMessage(Locale.ARROW_DAMAGE_INDICATOR.format(attacker,
                            range,
                            damaged.getName(),
                            health,
                            StringEscapeUtils.unescapeJava("❤")
                    ));
                }
                if (match.getGamePlayer(attacker).isRespawned() && match.getState().equals(MatchState.ENDING_MATCH)) {
                    event.setCancelled(true);
                }
                if (event.getDamager() instanceof Fireball) {
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntityMonitor(EntityDamageByEntityEvent event) {
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
                PlayerUtil.setLastAttacker(victim, attacker);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Match match = Profile.getByUuid(attacker.getUniqueId()).getMatch();
            if (match != null && match.getGamePlayer(attacker).isRespawned()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            if (event.getItem().hasItemMeta() &&
                    event.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
                Player player = event.getPlayer();
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
                player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getMatch() != null &&
                    profile.getMatch().getState() == MatchState.PLAYING_ROUND) {
                if (event.getFoodLevel() >= 20) {
                    event.setFoodLevel(20);
                    player.setSaturation(20);
                } else {
                    event.setCancelled(ThreadLocalRandom.current().nextInt(100) > 25);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

//    @EventHandler(priority = EventPriority.LOW)
//    public void onPlayerQuitEvent(PlayerQuitEvent event) {
//        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());
//
//        if (profile.getMatch() != null) {
//            Match match = profile.getMatch();
//
//            if (match != null && (match.getState() == MatchState.STARTING_ROUND || match.getState() == MatchState.PLAYING_ROUND)) {
//                profile.getMatch().onDisconnect(event.getPlayer());
//            }
//        }
//    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getState() == ProfileState.SPECTATING && event.getRightClicked() instanceof Player &&
                player.getItemInHand() != null) {
            Player target = (Player) event.getRightClicked();

            if (Practice.getInstance().getHotbar().fromItemStack(player.getItemInHand()) == HotbarItem.VIEW_INVENTORY) {
                new ViewInventoryMenu(target).openMenu(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();

        if (!(source instanceof Player)) return;
        Player shooter = (Player) source;
        Profile profile = Profile.getByUuid(shooter.getUniqueId());
        Match match = profile.getMatch();

        if (projectile instanceof EnderPearl) {
            if (match.getState() != MatchState.PLAYING_ROUND) {
                shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                event.setCancelled(true);
                return;
            }

            if (!profile.isEnderpearlOnCooldown()) {
                profile.setEnderpearlCooldown(new Cooldown(16_000));
            } else {
                event.setCancelled(true);
                shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                String time = TimeUtil.millisToSeconds(profile.getEnderpearlCooldown().getRemaining());
                shooter.sendMessage(Locale.MATCH_ENDERPEARL_COOLDOWN.format(shooter, time,
                        (time.equalsIgnoreCase("1.0") ? "" : "s")));
                shooter.updateInventory();
            }

        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (itemStack != null && (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getMatch() != null) {
                Match match = profile.getMatch();

                if (Practice.getInstance().getHotbar().fromItemStack(itemStack) == HotbarItem.SPECTATE_STOP) {
                    match.onDisconnect(player);
                    return;
                }

                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                    ItemStack kitItem = Practice.getInstance().getHotbar().getItems().get(HotbarItem.KIT_SELECTION);

                    if (itemStack.getType() == kitItem.getType() &&
                            itemStack.getDurability() == kitItem.getDurability()) {
                        Matcher matcher = HotbarItem.KIT_SELECTION.getPattern().
                                matcher(itemStack.getItemMeta().getDisplayName());

                        if (matcher.find()) {
                            String kitName = matcher.group(2);
                            KitLoadout kitLoadout = null;

                            if (kitName.equals("Default")) {
                                kitLoadout = match.getKit().getKitLoadout();
                            } else {
                                for (KitLoadout find : profile.getKitData().get(match.getKit()).getLoadouts()) {
                                    if (find != null && find.getCustomName().equals(kitName)) {
                                        kitLoadout = find;
                                    }
                                }
                            }

                            if (kitLoadout != null) {
                                player.sendMessage(Locale.MATCH_GIVE_KIT.format(player, kitLoadout.getCustomName(), match.getKit().getName()));
                                profile.getMatch().getGamePlayer(player).setKitLoadout(kitLoadout);
                                GameParticipant<MatchGamePlayer> participantA = match.getParticipantA();
                                player.getInventory().setArmorContents(InventoryUtil.color(kitLoadout.getArmor(), participantA.containsPlayer(player.getUniqueId()) ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));
                                player.getInventory().setContents(InventoryUtil.color(kitLoadout.getContents(), participantA.containsPlayer(player.getUniqueId()) ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));
                                player.updateInventory();
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }
}
