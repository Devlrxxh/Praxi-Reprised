package me.lrxh.practice.match;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.match.task.MatchLogicTask;
import me.lrxh.practice.match.task.MatchPearlCooldownTask;
import me.lrxh.practice.match.task.MatchSnapshotCleanupTask;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.participant.GamePlayer;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.profile.meta.ProfileKitData;
import me.lrxh.practice.profile.visibility.VisibilityLogic;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.util.*;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public abstract class Match {

    protected final Kit kit;
    protected final Arena arena;
    protected final boolean ranked;
    protected final List<MatchSnapshot> snapshots;
    protected final List<UUID> spectators;
    protected final List<Item> droppedItems;
    private final UUID matchId = UUID.randomUUID();
    private final Queue queue;
    private final List<Location> placedBlocks;
    private final List<BlockState> changedBlocks;
    protected boolean bedABroken;
    protected boolean bedBBroken;
    @Setter
    protected MatchState state = MatchState.STARTING_ROUND;
    protected long timeData;
    protected MatchLogicTask logicTask;
    private boolean duel;


    public Match(Queue queue, Kit kit, Arena arena, boolean ranked, boolean duel) {
        this.queue = queue;
        this.kit = kit;
        this.arena = arena;
        this.ranked = ranked;
        this.bedABroken = false;
        this.bedBBroken = false;
        this.snapshots = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.droppedItems = new ArrayList<>();
        this.placedBlocks = new ArrayList<>();
        this.changedBlocks = new ArrayList<>();
        this.duel = duel;
        Practice.getInstance().getCache().getMatches().add(this);
    }


    public static void init() {
        new MatchPearlCooldownTask().runTaskTimerAsynchronously(Practice.getInstance(), 2L, 2L);
        new MatchSnapshotCleanupTask().runTaskTimerAsynchronously(Practice.getInstance(), 20L * 5, 20L * 5);
    }

    public static void cleanup() {
        for (Match match : Practice.getInstance().getCache().getMatches()) {
            match.getPlacedBlocks().forEach(location -> location.getBlock().setType(Material.AIR));
            match.getChangedBlocks().forEach((blockState) -> blockState.getLocation().getBlock().setType(blockState.getType()));
            match.getDroppedItems().forEach(Entity::remove);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.save();
        }
    }

    public static int getInFightsCount(Queue queue) {
        int i = 0;

        for (Match match : Practice.getInstance().getCache().getMatches()) {
            if (match.getQueue() != null &&
                    (match.getState() == MatchState.STARTING_ROUND || match.getState() == MatchState.PLAYING_ROUND)) {
                if (match.getQueue().equals(queue)) {
                    for (GameParticipant<? extends GamePlayer> gameParticipant : match.getParticipants()) {
                        i += gameParticipant.getPlayers().size();
                    }
                }
            }
        }

        return i;
    }

    public static BaseComponent[] generateInventoriesComponents(String prefix, GameParticipant<MatchGamePlayer> participant) {
        return generateInventoriesComponents(prefix, Collections.singletonList(participant));
    }

    public static BaseComponent[] generateInventoriesComponents(String prefix, List<GameParticipant<MatchGamePlayer>> participants) {
        ChatComponentBuilder builder = new ChatComponentBuilder(prefix);

        int totalPlayers = 0;
        int processedPlayers = 0;

        for (GameParticipant<MatchGamePlayer> gameParticipant : participants) {
            totalPlayers += gameParticipant.getPlayers().size();
        }

        for (GameParticipant<MatchGamePlayer> gameParticipant : participants) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                processedPlayers++;

                ChatComponentBuilder current = new ChatComponentBuilder(
                        Locale.MATCH_CLICK_TO_VIEW_NAME.format(gamePlayer.getUsername()))
                        .attachToEachPart(ChatHelper.hover(Locale.MATCH_CLICK_TO_VIEW_HOVER.format(gamePlayer.getUsername())))
                        .attachToEachPart(ChatHelper.click("/viewinv " + gamePlayer.getUuid().toString()));

                builder.append(current.create());

                if (processedPlayers != totalPlayers) {
                    builder.append(", ");
                    builder.getCurrent().setClickEvent(null);
                    builder.getCurrent().setHoverEvent(null);
                }
            }
        }

        return builder.create();
    }

    public void setupPlayer(Player player) {
        // Set the player as alive
        MatchGamePlayer gamePlayer = getGamePlayer(player);
        gamePlayer.setDead(false);

        // If the player disconnected, skip any operations for them
        if (gamePlayer.isDisconnected()) {
            return;
        }

        // Reset the player's inventory
        PlayerUtil.reset(player);


        // Set the player's max damage ticks
        player.setMaximumNoDamageTicks(getKit().getGameRules().getHitDelay());

        // If the player has no kits, apply the default kit, otherwise
        // give the player a list of kit books to choose from
        if (!getKit().getGameRules().isSumo()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            ProfileKitData kitData = profile.getKitData().get(getKit());

            if (kitData.getKitCount() > 0) {
                profile.getKitData().get(getKit()).giveBooks(player);
            } else {
                GameParticipant<MatchGamePlayer> participantA = getParticipantA();
                player.getInventory().setArmorContents(InventoryUtil.color(getKit().getKitLoadout().getArmor(), participantA.containsPlayer(player.getUniqueId()) ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));
                //player.getInventory().setArmorContents(getKit().getKitLoadout().getArmor());
                player.getInventory().setContents(InventoryUtil.color(getKit().getKitLoadout().getContents(), participantA.containsPlayer(player.getUniqueId()) ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));
                //player.getInventory().setContents(getKit().getKitLoadout().getContents());
                player.sendMessage(Locale.MATCH_GIVE_KIT.format(player, "Default", getKit().getName()));
                profile.getMatch().getGamePlayer(player).setKitLoadout(getKit().getKitLoadout());
            }
        }
    }

    public Player getOpponent(UUID playerUUID) {
        GameParticipant<MatchGamePlayer> playerParticipant = getParticipant(Bukkit.getPlayer(playerUUID));
        if (playerParticipant != null) {
            for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
                if (!gameParticipant.equals(playerParticipant)) {
                    for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                        if (!gamePlayer.isDisconnected()) {
                            return gamePlayer.getPlayer();
                        }
                    }
                }
            }
        }
        return null;
    }


    public void start() {
        // Set state
        state = MatchState.STARTING_ROUND;

        // Start logic task
        logicTask = new MatchLogicTask(this);
        logicTask.runTaskTimer(Practice.getInstance(), 0L, 20L);

        // Set arena as active
        arena.setActive(true);

        // Send arena message
        sendMessage(Locale.MATCH_PLAYING_ARENA.format(arena.getDisplayName()));

        // Setup players
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    Profile profile = Profile.getByUuid(player.getUniqueId());
                    profile.setState(ProfileState.FIGHTING);
                    profile.setMatch(this);
                    profile.getDuelRequests().clear();
                    setupPlayer(player);
                }
            }
        }

        // Handle player visibility
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    VisibilityLogic.handle(player);
                }
            }
        }

        if (kit.getGameRules().isBuild()) {
            arena.takeSnapshot();
        }
    }

    public void end() {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                Player player = gamePlayer.getPlayer();
                if (!gamePlayer.isDisconnected()) {
                    if (player != null) {
                        player.setFireTicks(0);
                        player.updateInventory();

                        Profile profile = Profile.getByUuid(player.getUniqueId());
                        profile.setState(ProfileState.LOBBY);
                        profile.setMatch(null);
                        profile.setEnderpearlCooldown(new Cooldown(0));
                        PlayerUtil.allowMovement(gamePlayer.getPlayer());
                        player.sendMessage(CC.translate("&cEnemy &7committed suicide."));
                        VisibilityLogic.handle(player);
                        Practice.getInstance().getHotbar().giveHotbarItems(player);
                        PlayerUtil.teleportToSpawn(player);
                        PlayerUtil.allowMovement(gamePlayer.getPlayer());
                    }
                }
            }
        }

        for (Player player : getSpectatorsAsPlayers()) {
            removeSpectator(player);
        }


        if (kit.getGameRules().isBuild()) {
            arena.restoreSnapshot();

            arena.setActive(false);
        }

        Practice.getInstance().getCache().getMatches().remove(this);
    }

    public abstract boolean canEndMatch();

    public void onRoundStart() {
        // Reset snapshots
        snapshots.clear();
        timeData = System.currentTimeMillis() - timeData;
        checkFollowers();
        // Reset each game participant
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            gameParticipant.reset();
//            if (Practice.getInstance().getSpigotHandler() == null) return;
//            if (kit.getKnockbackProfile() == null) return;
//            for (GamePlayer gamePlayer : gameParticipant.getPlayers()) {
//                Practice.getInstance().getSpigotHandler().getKnockback().setKnockback(gamePlayer.getPlayer(), kit.getKnockbackProfile());
//            }
        }
    }

    public abstract boolean canStartRound();

    public void onRoundEnd() {
        // Snapshot alive players' inventories
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player player = gamePlayer.getPlayer();

                    if (player != null) {
                        if (!gamePlayer.isDead()) {
                            MatchSnapshot snapshot = new MatchSnapshot(player, false);
                            snapshot.setPotionsThrown(gamePlayer.getPotionsThrown());
                            snapshot.setPotionsMissed(gamePlayer.getPotionsMissed());
                            snapshot.setLongestCombo(gamePlayer.getLongestCombo());
                            snapshot.setTotalHits(gamePlayer.getHits());

                            snapshots.add(snapshot);
                        }
                    }
                }
            }
        }

        // Make all snapshots available
        for (MatchSnapshot snapshot : snapshots) {
            snapshot.setCreatedAt(System.currentTimeMillis());
            MatchSnapshot.getSnapshots().put(snapshot.getUuid(), snapshot);
        }

        List<BaseComponent[]> endingMessages = generateEndComponents();

        // Send ending messages to game participants
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player player = gamePlayer.getPlayer();

                    if (player != null) {
                        for (BaseComponent[] components : endingMessages) {
                            player.sendMessage(components);
                        }
                    }
                }
            }
        }

        // Send ending messages to spectators
        for (Player player : getSpectatorsAsPlayers()) {
            for (BaseComponent[] components : endingMessages) {
                player.spigot().sendMessage(components);
            }

            removeSpectator(player);
        }
    }

    public void respawn(UUID playerUUID) {
        Profile profile = Profile.getByUuid(playerUUID);
        Player player = Bukkit.getPlayer(playerUUID);
        MatchGamePlayer gamePlayer = profile.getMatch().getGamePlayer(player);
        if (gamePlayer.isRespawned()) {
            return;
        }
        if (state != MatchState.PLAYING_ROUND) {
            return;
        }
        hidePlayer(playerUUID);
        gamePlayer.setRespawned(true);

        sendDeathMessage(player, PlayerUtil.getLastAttacker(player), false);
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0));

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        player.setVelocity(player.getVelocity().add(new Vector(0, 0.25, 0)));
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setVelocity(player.getVelocity().add(new Vector(0, 0.15, 0)));
        player.setAllowFlight(true);
        player.setFlying(true);
        new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (countdown > 0) {
                    player.sendMessage(Locale.MATCH_RESPAWN_TIMER.format(player, countdown));
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 10, 1);
                    if (!gamePlayer.isRespawned()) {
                        gamePlayer.setRespawned(false);
                        this.cancel();
                    }
                    countdown--;
                } else {
                    player.sendMessage(Locale.MATCH_RESPAWNED.format(player));
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 1);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    boolean aTeam = getParticipantA().containsPlayer(player.getUniqueId());
                    Location spawn = aTeam ? getArena().getSpawnA() : getArena().getSpawnB();
                    player.teleport(spawn);
                    player.getInventory().setArmorContents(InventoryUtil.color(gamePlayer.getKitLoadout().getArmor(), aTeam ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));
                    //player.getInventory().setContents(gamePlayer.getKitLoadout().getContents());
                    player.getInventory().setContents(InventoryUtil.color(gamePlayer.getKitLoadout().getContents(), aTeam ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));

                    player.setGameMode(GameMode.SURVIVAL);
                    gamePlayer.setRespawned(false);
                    PlayerUtil.setImmune(player, 30);
                    showPlayer(playerUUID);
                    this.cancel();
                }
            }
        }.runTaskTimer(Practice.getInstance(), 0L, 20L);


    }

    public void broadcast(String message) {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                gamePlayer.getPlayer().sendMessage(message);
            }
        }

        for (Player player : getSpectatorsAsPlayers()) {
            player.sendMessage(message);
        }
    }

    public void hidePlayer(UUID playerUUID) {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                gamePlayer.getPlayer().hidePlayer(Bukkit.getPlayer(playerUUID));
            }
        }

        for (Player player : getSpectatorsAsPlayers()) {
            player.hidePlayer(Bukkit.getPlayer(playerUUID));
        }
    }

    public void showPlayer(UUID playerUUID) {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                gamePlayer.getPlayer().showPlayer(Bukkit.getPlayer(playerUUID));
            }
        }

        for (Player player : getSpectatorsAsPlayers()) {
            player.showPlayer(Bukkit.getPlayer(playerUUID));
        }
    }

    public abstract boolean canEndRound();

    public void onDisconnect(Player dead) {
        // Don't continue if the match is already ending
        if (!(state == MatchState.STARTING_ROUND || state == MatchState.PLAYING_ROUND)) {
            return;
        }

        MatchGamePlayer deadGamePlayer = getGamePlayer(dead);

        if (deadGamePlayer != null) {
            deadGamePlayer.setDisconnected(true);

            if (!deadGamePlayer.isDead()) {
                onDeath(dead);
            }
        }
        end();
        sendDeathMessage(dead, null, false);
    }

    public void onDeath(Player dead) {
        // Don't continue if the match is already ending
        if (!(state == MatchState.STARTING_ROUND || state == MatchState.PLAYING_ROUND)) {
            return;
        }
        dead.getInventory().setContents(new ItemStack[36]);
        PlayerUtil.animateDeath(dead);

        MatchGamePlayer deadGamePlayer = getGamePlayer(dead);
        Player killer = PlayerUtil.getLastAttacker(dead);


        // Don't continue if the player is already dead
        if (deadGamePlayer.isDead()) {
            return;
        }

        // Set player as dead
        deadGamePlayer.setDead(true);

        // Get killer
        if (killer != null) {
            Profile killerProfile = Profile.getByUuid(killer.getUniqueId());
            Location location = dead.getLocation();
            World world = location.getWorld();
            switch (killerProfile.getOptions().killEffect()) {
                case LIGHTNING:
                    double x = location.getX();
                    double y = location.getY() + 2.0;
                    double z = location.getZ();
                    Location lightningLocation = new Location(world, x, y, z);
                    world.strikeLightning(lightningLocation);
                    break;
                case FIREWORKS:
                    Firework firework = world.spawn(location, Firework.class);
                    FireworkMeta meta = firework.getFireworkMeta();
                    FireworkEffect.Builder builder = FireworkEffect.builder()
                            .withColor(Color.RED)
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .trail(true)
                            .flicker(false);
                    meta.addEffect(builder.build());
                    meta.setPower(1);
                    firework.setFireworkMeta(meta);
                    Bukkit.getScheduler().runTaskLater(Practice.getInstance(), firework::detonate, 5L);
                    break;
            }
        }
        dead.setHealth(dead.getMaxHealth());
        dead.setFoodLevel(20);
        dead.setVelocity(dead.getVelocity().add(new Vector(0, 0.25, 0)));
        dead.setAllowFlight(true);
        dead.setFlying(true);
        dead.setVelocity(dead.getVelocity().add(new Vector(0, 0.15, 0)));
        dead.setAllowFlight(true);
        dead.setFlying(true);

        // Store snapshot of player inventory and stats
        MatchSnapshot snapshot = new MatchSnapshot(dead, true);
        snapshot.setPotionsMissed(deadGamePlayer.getPotionsMissed());
        snapshot.setPotionsThrown(deadGamePlayer.getPotionsThrown());
        snapshot.setLongestCombo(deadGamePlayer.getLongestCombo());
        snapshot.setTotalHits(deadGamePlayer.getHits());
        Profile loserProfile = Profile.getByUuid(deadGamePlayer.getUuid());
        if (!Practice.getInstance().getCache().getMatch(matchId).isDuel()) {
            loserProfile.getKitData().get(loserProfile.getMatch().getKit()).incrementLost();
        }
        // Add snapshot to list
        snapshots.add(snapshot);

        // Reset inventory
        PlayerUtil.reset(dead);

        // Handle visibility for match players
        // Send death message
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player player = gamePlayer.getPlayer();

                    if (player != null) {
                        VisibilityLogic.handle(player, dead);
                        if (player != dead) {
                            Profile winnerProfile = Profile.getByUuid(player.getUniqueId());
                            if (!Practice.getInstance().getCache().getMatch(matchId).isDuel()) {
                                winnerProfile.getKitData().get(winnerProfile.getMatch().getKit()).incrementWon();
                            }
                        }
                    }
                }
            }
        }

        if (kit.getGameRules().isBedwars()) {
            sendDeathMessage(dead, killer, true);
        }

        // Handle visibility for spectators
        // Send death message
        for (Player player : getSpectatorsAsPlayers()) {
            VisibilityLogic.handle(player, dead);
        }


        if (canEndRound()) {
            state = MatchState.ENDING_ROUND;
            timeData = System.currentTimeMillis() - timeData;
            onRoundEnd();

            if (canEndMatch()) {
                state = MatchState.ENDING_MATCH;
                logicTask.setNextAction(4);
            } else {
                logicTask.setNextAction(4);
            }
        } else {
            dead.setAllowFlight(true);
            dead.setFlying(true);

            Practice.getInstance().getHotbar().giveHotbarItems(dead);
        }

    }

    public abstract boolean isOnSameTeam(Player first, Player second);

    public abstract List<GameParticipant<MatchGamePlayer>> getParticipants();

    public GameParticipant<MatchGamePlayer> getParticipant(Player player) {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            if (gameParticipant.containsPlayer(player.getUniqueId())) {
                return gameParticipant;
            }
        }

        return null;
    }

    public GameParticipant<MatchGamePlayer> getParticipantA() {
        return getParticipants().get(0);
    }

    public GameParticipant<MatchGamePlayer> getParticipantB() {
        return getParticipants().get(1);
    }

    public MatchGamePlayer getGamePlayer(Player player) {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (gamePlayer.getUuid().equals(player.getUniqueId())) {
                    return gamePlayer;
                }
            }
        }

        return null;
    }

    public abstract ChatColor getRelationColor(Player viewer, Player target);

    public abstract List<String> getScoreboardLines(Player player);

    public void addSpectator(Player spectator, Player target) {
        spectators.add(spectator.getUniqueId());

        Profile profile = Profile.getByUuid(spectator.getUniqueId());
        profile.setMatch(this);
        profile.setState(ProfileState.SPECTATING);

        Practice.getInstance().getHotbar().giveHotbarItems(spectator);

        spectator.teleport(target.getLocation().clone().add(0, 2, 0));
        spectator.setGameMode(GameMode.SURVIVAL);
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
        spectator.updateInventory();

        VisibilityLogic.handle(spectator);

        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (GamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player bukkitPlayer = gamePlayer.getPlayer();

                    if (bukkitPlayer != null) {
                        VisibilityLogic.handle(bukkitPlayer);
                        if (!profile.isSilent()) {
                            bukkitPlayer.sendMessage(Locale.MATCH_NOW_SPECTATING.format(bukkitPlayer, spectator.getName()));
                        }
                    }
                }
            }
        }
    }

    public void removeSpectator(Player spectator) {
        spectators.remove(spectator.getUniqueId());

        Profile profile = Profile.getByUuid(spectator.getUniqueId());
        profile.setState(ProfileState.LOBBY);
        profile.setMatch(null);

        PlayerUtil.reset(spectator);
        Practice.getInstance().getHotbar().giveHotbarItems(spectator);
        PlayerUtil.teleportToSpawn(spectator);

        VisibilityLogic.handle(spectator);

        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player bukkitPlayer = gamePlayer.getPlayer();

                    if (bukkitPlayer != null) {
                        VisibilityLogic.handle(bukkitPlayer);

                        if (state != MatchState.ENDING_MATCH) {
                            bukkitPlayer.sendMessage(Locale.MATCH_NO_LONGER_SPECTATING.format(bukkitPlayer, spectator.getName()));
                        }
                    }
                }
            }
        }
    }

    public String getDuration() {
        if (state.equals(MatchState.STARTING_ROUND)) {
            return "00:00";
        } else if (state.equals(MatchState.ENDING_ROUND)) {
            return "Ending";
        } else if (state.equals(MatchState.PLAYING_ROUND)) {
            return TimeUtil.millisToTimer(System.currentTimeMillis() - this.timeData);
        }
        return "Ending";
    }

    public void sendMessage(String message) {
        for (GameParticipant gameParticipant : getParticipants()) {
            gameParticipant.sendMessage(message);
        }

        for (Player player : getSpectatorsAsPlayers()) {
            ArrayList<String> list = new ArrayList<>();
            list.add(CC.translate(message));
            player.sendMessage(PlaceholderUtil.format(list, player).toString().replace("[", "").replace("]", ""));
        }
    }

    public void sendSound(Sound sound, float volume, float pitch) {
        for (GameParticipant gameParticipant : getParticipants()) {
            gameParticipant.sendSound(sound, volume, pitch);
        }

        for (Player player : getSpectatorsAsPlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void checkFollowers() {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (GamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (!Profile.getByUuid(gamePlayer.getUuid()).getFollowers().isEmpty()) {
                    for (UUID playerUUID : Profile.getByUuid(gamePlayer.getUuid()).getFollowers()) {
                        Bukkit.getPlayer(playerUUID).chat("/spec " + gamePlayer.getUsername());
                    }
                }
            }
        }
    }

    protected List<Player> getSpectatorsAsPlayers() {
        List<Player> players = new ArrayList<>();

        for (UUID uuid : spectators) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                players.add(player);
            }
        }

        return players;
    }

    public abstract List<BaseComponent[]> generateEndComponents();


    public void sendDeathMessage(Player dead, Player killer, boolean finalKill) {
        String deathMessage;
        if (finalKill) {
            for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
                for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                    Player player = gamePlayer.getPlayer();
                    if (killer == null) {
                        deathMessage = Locale.MATCH_PLAYER_FINAL_KILL.format(player,
                                getRelationColor(player, dead) + dead.getName()
                        );
                    } else {
                        deathMessage = Locale.MATCH_PLAYER_FINAL_KILL.format(player,
                                getRelationColor(player, dead) + dead.getName(),
                                getRelationColor(player, killer) + killer.getName()
                        );
                    }
                    player.sendMessage(deathMessage);
                }
            }

            for (Player player : getSpectatorsAsPlayers()) {
                if (killer == null) {
                    deathMessage = Locale.MATCH_PLAYER_FINAL_KILL.format(player,
                            getRelationColor(player, dead) + dead.getName()
                    );
                } else {
                    deathMessage = Locale.MATCH_PLAYER_FINAL_KILL.format(player,
                            getRelationColor(player, dead) + dead.getName(),
                            getRelationColor(player, killer) + killer.getName()
                    );
                }
                player.sendMessage(deathMessage);
            }
            return;
        }

        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                Player player = gamePlayer.getPlayer();
                if (killer == null) {
                    deathMessage = Locale.MATCH_PLAYER_DIED.format(player,
                            getRelationColor(player, dead) + dead.getName()
                    );
                } else {
                    deathMessage = Locale.MATCH_PLAYER_KILLED.format(player,
                            getRelationColor(player, dead) + dead.getName(),
                            getRelationColor(player, killer) + killer.getName()
                    );
                }
                player.sendMessage(deathMessage);
            }
        }

        for (Player player : getSpectatorsAsPlayers()) {
            if (killer == null) {
                deathMessage = Locale.MATCH_PLAYER_DIED.format(player,
                        getRelationColor(player, dead) + dead.getName()
                );
            } else {
                deathMessage = Locale.MATCH_PLAYER_KILLED.format(player,
                        getRelationColor(player, dead) + dead.getName(),
                        getRelationColor(player, killer) + killer.getName()
                );
            }
            player.sendMessage(deathMessage);
        }

    }

}
