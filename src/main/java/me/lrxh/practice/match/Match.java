package me.lrxh.practice.match;

import lombok.Getter;
import lombok.Setter;
import me.jumper251.replay.api.ReplayAPI;
import me.jumper251.replay.filesystem.saving.ReplaySaver;
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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public static ChatComponentBuilder[] getTeamAsComponent(GameParticipant<MatchGamePlayer> participants) {
        List<ChatComponentBuilder> chatComponentBuilders = new ArrayList<>();

        for (MatchGamePlayer matchGamePlayer : participants.getPlayers()) {
            ChatComponentBuilder current = new ChatComponentBuilder(
                    Locale.MATCH_CLICK_TO_VIEW_NAME.format(matchGamePlayer.getUsername()))
                    .attachToEachPart(ChatHelper.hover(Locale.MATCH_CLICK_TO_VIEW_HOVER.format(matchGamePlayer.getUsername())))
                    .attachToEachPart(ChatHelper.click("/viewinv " + matchGamePlayer.getUuid().toString()));
            chatComponentBuilders.add(current);
        }
        return chatComponentBuilders.toArray(new ChatComponentBuilder[0]);
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
        Profile profile = Profile.getByUuid(player.getUniqueId());

        // Reset the player's inventory
        PlayerUtil.reset(player);

        if (Practice.getInstance().isReplay() && !kit.getGameRules().isBuild()) {
                if (ReplaySaver.exists(profile.getUuid().toString())) {
                    ReplaySaver.delete(profile.getUuid().toString());
            }
        }

        // Set the player's max damage ticks
        player.setMaximumNoDamageTicks(getKit().getGameRules().getHitDelay());

        // If the player has no kits, apply the default kit, otherwise
        // give the player a list of kit books to choose from
        if (!getKit().getGameRules().isSumo()) {
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
        if (playerParticipant == null) {
            return null;
        }

        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            if (!gameParticipant.equals(playerParticipant)) {
                for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                    if (!gamePlayer.isDisconnected()) {
                        return gamePlayer.getPlayer();
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
        for (Player player : getPlayers()) {
            if (player != null) {
                Profile profile = Profile.getByUuid(player.getUniqueId());
                profile.setState(ProfileState.FIGHTING);
                profile.setMatch(this);
                profile.getDuelRequests().clear();
                setupPlayer(player);
                if (Practice.getInstance().isReplay()) {
                    ReplayAPI.getInstance().recordReplay
                            (profile.getUuid().toString(), getParticipants().stream().flatMap(participant -> participant.getPlayers().stream())
                                    .filter(gamePlayer -> !gamePlayer.isDisconnected()).map(MatchGamePlayer::getPlayer)
                                    .collect(Collectors.toList()));

                }
            }
        }

        // Handle player visibility
        for (Player player : getPlayers()) {
            if (player != null) {
                VisibilityLogic.handle(player);
            }
        }

        if (kit.getGameRules().isBuild()) {
            arena.takeSnapshot();
        }
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer matchGamePlayer : gameParticipant.getPlayers()) {
                players.add(matchGamePlayer.getPlayer());
            }
        }
        return players;
    }


    public void end() {

        for (Player player : getPlayers()) {
            if (player != null) {
                player.setFireTicks(0);
                player.updateInventory();

                Profile profile = Profile.getByUuid(player.getUniqueId());
                profile.setState(ProfileState.LOBBY);
                profile.setEnderpearlCooldown(new Cooldown(0));
                PlayerUtil.allowMovement(player);
                VisibilityLogic.handle(player);
                Practice.getInstance().getHotbar().giveHotbarItems(player);
                PlayerUtil.teleportToSpawn(player);
                PlayerUtil.allowMovement(player);
                Objective objective = player.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);
                profile.setMatch(null);
                if (objective != null) {
                    objective.unregister();
                }
                if (Practice.getInstance().isReplay() && !kit.getGameRules().isBuild()) {
                    ReplayAPI.getInstance().stopReplay(profile.getUuid().toString(), true, true);
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


        // Send ending messages to game participants
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player player = gamePlayer.getPlayer();
                    sendEndMessage(player);
                }
            }
        }

        // Send ending messages to spectators
        for (Player player : getSpectatorsAsPlayers()) {
            sendEndMessage(player);
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

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        PlayerUtil.doVelocityChange(player);

        new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (countdown > 0) {
                    player.sendMessage(Locale.MATCH_RESPAWN_TIMER.format(player, countdown));
                    PlayerUtil.sendTitle(player, CC.translate("&c" + countdown), "", 20);
                    if (!gamePlayer.isRespawned()) {
                        gamePlayer.setRespawned(false);
                        this.cancel();
                    }
                    countdown--;
                } else {
                    player.sendMessage(Locale.MATCH_RESPAWNED.format(player));
                    player.playSound(player.getLocation(), Sound.FALL_BIG, 1.0f, 1.0f);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    boolean aTeam = getParticipantA().containsPlayer(player.getUniqueId());
                    Location spawn = aTeam ? getArena().getSpawnA() : getArena().getSpawnB();
                    player.teleport(spawn);
                    player.getInventory().setArmorContents(InventoryUtil.color(gamePlayer.getKitLoadout().getArmor(), aTeam ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));
                    //player.getInventory().setContents(gamePlayer.getKitLoadout().getContents());
                    player.getInventory().setContents(InventoryUtil.color(gamePlayer.getKitLoadout().getContents(), aTeam ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));
                    player.setGameMode(GameMode.SURVIVAL);
                    showPlayer(playerUUID);
                    gamePlayer.setRespawned(false);
                    PlayerUtil.setImmune(player, 40);
                    PlayerUtil.sendTitle(player, CC.translate("&aRespawned!"), "", 20);
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
    }

    public void onDeath(Player dead) {
        // Don't continue if the match is already ending
        if (!(state == MatchState.STARTING_ROUND || state == MatchState.PLAYING_ROUND)) {
            return;
        }

        MatchGamePlayer deadGamePlayer = getGamePlayer(dead);
        Player killer = PlayerUtil.getLastAttacker(dead);

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
        dead.setItemInHand(null);

        // Add snapshot to list
        snapshots.add(snapshot);

        PlayerUtil.animateDeath(dead);

        if (killer != null) {
            PlayerUtil.sendTitle(dead, CC.translate("&cDEFEAT!"), "&a" + killer.getName() + " &7won the match!", 70);
        } else {
            PlayerUtil.sendTitle(dead, CC.translate("&cDEFEAT!"), "&aEnemy" + " &7won the match!", 70);
        }

        // Don't continue if the player is already dead
        if (deadGamePlayer.isDead()) {
            return;
        }

        // Set player as dead
        deadGamePlayer.setDead(true);

        // Get killer
        if (killer != null) {
            Profile killerProfile = Profile.getByUuid(killer.getUniqueId());
            killerProfile.getOptions().killEffect().execute(killer, dead.getLocation());
            PlayerUtil.sendTitle(killer, CC.translate("&aVICTORY!"), "&aYou" + " &7won the match!", 70);
            killer.playSound(killer.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
            PlayerUtil.doVelocityChange(killer);
        }

        PlayerUtil.setLastAttacker(dead, null);

        PlayerUtil.reset(dead);

        PlayerUtil.doVelocityChange(dead);


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

    public void sendTeamAMessage(String message) {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            gameParticipant.sendMessage(message);
        }
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

    public void sendTitle(String header, String footer, int duration) {
        for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
            for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
                PlayerUtil.sendTitle(gamePlayer.getPlayer(), header, footer, duration);
            }
        }
    }

    public void sendTitleA(String header, String footer, int duration) {
        getParticipantA().sendTitle(header, footer, duration);
    }

    public void sendTitleB(String header, String footer, int duration) {
        getParticipantB().sendTitle(header, footer, duration);
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

    public abstract void sendEndMessage(Player player);


    public void sendDeathMessage(Player dead, Player killer, boolean finalKill) {
        String deathMessage;
        Locale deathLocale;

        if (finalKill) {
            deathLocale = (killer == null) ? Locale.MATCH_PLAYER_DIED_FINAL : Locale.MATCH_PLAYER_FINAL_KILL;
        } else {
            deathLocale = (killer == null) ? Locale.MATCH_PLAYER_DIED : Locale.MATCH_PLAYER_KILLED;
        }

        for (Player player : getPlayers()) {
            deathMessage = formatDeathMessage(deathLocale, player, dead, killer);
            player.sendMessage(deathMessage);
        }

        for (Player player : getSpectatorsAsPlayers()) {
            deathMessage = formatDeathMessage(deathLocale, player, dead, killer);
            player.sendMessage(deathMessage);
        }
    }

    private String formatDeathMessage(Locale deathLocale, Player player, Player dead, Player killer) {
        String deathMessage;
        if (killer == null) {
            deathMessage = deathLocale.format(player,
                    getRelationColor(player, dead) + dead.getName()
            );
        } else {
            deathMessage = deathLocale.format(player,
                    getRelationColor(player, dead) + dead.getName(),
                    getRelationColor(player, killer) + killer.getName()
            );
        }
        return deathMessage;
    }
}
