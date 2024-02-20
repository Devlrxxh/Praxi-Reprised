package me.lrxh.practice.match;

import me.lrxh.practice.Locale;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.kit.KitLoadout;
import me.lrxh.practice.match.menu.ViewInventoryMenu;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.profile.hotbar.Hotbar;
import me.lrxh.practice.profile.hotbar.HotbarItem;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.Cooldown;
import me.lrxh.practice.util.PlayerUtil;
import me.lrxh.practice.util.TimeUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;

public class MatchListener implements Listener {


    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());


        if (profile.getState() == ProfileState.FIGHTING) {
            if (profile.getMatch().getKit().getGameRules().isSumo() ||
                    profile.getMatch().getKit().getGameRules().isSpleef()) {
                Match match = profile.getMatch();
                Location playerLocation = event.getPlayer().getLocation();
                Block block = playerLocation.getBlock();

                if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                    match.onDeath(event.getPlayer());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getState() == ProfileState.FIGHTING) {
            Match match = profile.getMatch();

            if (match.getKit().getGameRules().isBuild() && match.getState() == MatchState.PLAYING_ROUND) {
                if (match.getKit().getGameRules().isSpleef()) {
                    event.setCancelled(true);
                    return;
                }

                Arena arena = match.getArena();
                int x = (int) event.getBlockPlaced().getLocation().getX();
                int y = (int) event.getBlockPlaced().getLocation().getY();
                int z = (int) event.getBlockPlaced().getLocation().getZ();

                if (y > arena.getMaxBuildHeight()) {
                    event.getPlayer().sendMessage(CC.RED + "You have reached the maximum build height.");
                    event.setCancelled(true);
                    return;
                }

                if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() &&
                        z >= arena.getZ1() && z <= arena.getZ2()) {
                    match.getPlacedBlocks().add(event.getBlock().getLocation());
                } else {
                    event.getPlayer().sendMessage(CC.RED + "You cannot build outside of the arena!");
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
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getState() == ProfileState.FIGHTING) {
            Match match = profile.getMatch();

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
        } else {
            if (!event.getPlayer().isOp() || event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getState() == ProfileState.FIGHTING) {
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

        if (profile.getState() == ProfileState.FIGHTING) {
            if (profile.getMatch().getGamePlayer(event.getPlayer()).isDead()) {
                event.setCancelled(true);
                return;
            }

            if (event.getItem().getItemStack().getType().name().contains("BOOK")) {
                event.setCancelled(true);
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

        if (profile.getState() == ProfileState.FIGHTING) {
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
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.getDrops().clear();

        Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

        if (profile.getState() == ProfileState.FIGHTING) {
            List<Item> entities = new ArrayList<>();

            event.getDrops().forEach(itemStack -> {
                if (!(itemStack.getType() == Material.BOOK || itemStack.getType() == Material.ENCHANTED_BOOK)) {
                    entities.add(event.getEntity().getLocation().getWorld()
                            .dropItemNaturally(event.getEntity().getLocation(), itemStack));
                }
            });

            profile.getMatch().getDroppedItems().addAll(entities);
            profile.getMatch().onDeath(event.getEntity());
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

            if (profile.getState() == ProfileState.FIGHTING) {
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

            if (shooterData.getState() == ProfileState.FIGHTING &&
                    shooterData.getMatch().getState() == MatchState.PLAYING_ROUND) {
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

                if (profile.getState() == ProfileState.FIGHTING && !profile.getMatch().getKit().getGameRules().isHealthRegeneration()) {
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

            if (profile.getState() == ProfileState.FIGHTING) {
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    profile.getMatch().onDeath(player);
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
        if (!profile.getState().equals(ProfileState.FIGHTING)) return;
        if (profile.getMatch().getKit().getGameRules().isSumo()
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

            if (profile.getState() == ProfileState.FIGHTING &&
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

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());

        if (profile.getState() == ProfileState.FIGHTING) {
            Match match = profile.getMatch();

            if (match.getState() == MatchState.STARTING_ROUND || match.getState() == MatchState.PLAYING_ROUND) {
                profile.getMatch().onDisconnect(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getState() == ProfileState.SPECTATING && event.getRightClicked() instanceof Player &&
                player.getItemInHand() != null) {
            Player target = (Player) event.getRightClicked();

            if (Hotbar.fromItemStack(player.getItemInHand()) == HotbarItem.VIEW_INVENTORY) {
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

            if (profile.getState() == ProfileState.FIGHTING) {
                Match match = profile.getMatch();

                if (Hotbar.fromItemStack(itemStack) == HotbarItem.SPECTATE_STOP) {
                    match.onDisconnect(player);
                    return;
                }

                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                    ItemStack kitItem = Hotbar.getItems().get(HotbarItem.KIT_SELECTION);

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
                                player.sendMessage(Locale.MATCH_GIVE_KIT.format(player, kitLoadout.getCustomName()));
                                player.getInventory().setArmorContents(kitLoadout.getArmor());
                                player.getInventory().setContents(kitLoadout.getContents());
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
