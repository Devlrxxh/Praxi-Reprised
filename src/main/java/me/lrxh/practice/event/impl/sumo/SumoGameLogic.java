package me.lrxh.practice.event.impl.sumo;

import lombok.Getter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.event.game.EventGame;
import me.lrxh.practice.event.game.EventGameLogic;
import me.lrxh.practice.event.game.EventGameLogicTask;
import me.lrxh.practice.event.game.EventGameState;
import me.lrxh.practice.event.game.map.EventGameMap;
import me.lrxh.practice.event.game.map.vote.EventGameMapVoteData;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.participant.GamePlayer;
import me.lrxh.practice.participant.TeamGameParticipant;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.profile.hotbar.HotbarItem;
import me.lrxh.practice.profile.visibility.VisibilityLogic;
import me.lrxh.practice.util.BlockUtil;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.Cooldown;
import me.lrxh.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SumoGameLogic implements EventGameLogic {

    private final EventGame game;
    @Getter
    private final EventGameLogicTask logicTask;
    @Getter
    private GameParticipant<GamePlayer> participantA;
    @Getter
    private GameParticipant<GamePlayer> participantB;
    @Getter
    private int roundNumber;
    private GameParticipant winningParticipant;

    SumoGameLogic(EventGame game) {
        this.game = game;
        this.logicTask = new EventGameLogicTask(game);
        this.logicTask.runTaskTimer(Practice.getInstance(), 0, 20L);
    }


    @Override
    public EventGameLogicTask getGameLogicTask() {
        return logicTask;
    }

    @Override
    public void startEvent() {
        List<String> lines = Locale.EVENT_START.formatLines(game.getEvent().getDisplayName(),
                game.getParticipants().size(), game.getMaximumPlayers());

        for (String line : lines) {
            Bukkit.broadcastMessage(line);
        }

        int chosenMapVotes = 0;

        for (Map.Entry<EventGameMap, EventGameMapVoteData> entry : game.getVotesData().entrySet()) {
            if (game.getGameMap() == null) {
                game.setGameMap(entry.getKey());
                chosenMapVotes = entry.getValue().getPlayers().size();
            } else {
                if (entry.getValue().getPlayers().size() >= chosenMapVotes) {
                    game.setGameMap(entry.getKey());
                    chosenMapVotes = entry.getValue().getPlayers().size();
                }
            }
        }

        for (GameParticipant<GamePlayer> participant : game.getParticipants()) {
            for (GamePlayer gamePlayer : participant.getPlayers()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    PlayerUtil.reset(player);
                    player.teleport(game.getGameMap().getSpectatorPoint());
                    Practice.getInstance().getHotbar().giveHotbarItems(player);
                }
            }
        }
    }

    @Override
    public boolean canStartEvent() {
        return (game.getMaximumPlayers() == 2 ? game.getRemainingParticipants() >= 2 :
                game.getRemainingParticipants() >= (game.getMaximumPlayers() / 2));
    }

    @Override
    public void preEndEvent() {
        for (GameParticipant participant : game.getParticipants()) {
            if (!participant.isEliminated()) {
                winningParticipant = participant;
                break;
            }
        }

        if (winningParticipant != null) {
            List<String> lines = Locale.EVENT_FINISH.formatLines(game.getEvent().getDisplayName(),
                    winningParticipant.getConjoinedNames(),
                    (winningParticipant.getPlayers().size() == 1 ? "has" : "have"),
                    game.getEvent().getDisplayName());

            for (String line : lines) {
                Bukkit.broadcastMessage(line);
            }
        }
    }

    @Override
    public void endEvent() {
        EventGame.setActiveGame(null);
        EventGame.setCooldown(new Cooldown(60_000L * 3L));

        for (GameParticipant<GamePlayer> participant : game.getParticipants()) {
            for (GamePlayer gamePlayer : participant.getPlayers()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    Profile profile = Profile.getByUuid(player.getUniqueId());
                    profile.setState(ProfileState.LOBBY);

                    Practice.getInstance().getHotbar().giveHotbarItems(player);
                    Practice.getInstance().getEssentials().teleportToSpawn(player);
                    VisibilityLogic.handle(player);
                }
            }
        }
    }

    @Override
    public boolean canEndEvent() {
        return game.getRemainingParticipants() <= 1;
    }

    @Override
    public void cancelEvent() {
        game.sendMessage(ChatColor.DARK_RED + "The event has been cancelled by an administrator!");

        EventGame.setActiveGame(null);
        EventGame.setCooldown(new Cooldown(60_000L * 3L));

        for (GameParticipant<GamePlayer> participant : game.getParticipants()) {
            for (GamePlayer gamePlayer : participant.getPlayers()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    Profile profile = Profile.getByUuid(player.getUniqueId());
                    profile.setState(ProfileState.LOBBY);

                    Practice.getInstance().getHotbar().giveHotbarItems(player);

                    Practice.getInstance().getEssentials().teleportToSpawn(player);
                }
            }
        }
    }

    @Override
    public void preStartRound() {
        roundNumber++;

        GameParticipant<GamePlayer>[] participants = findParticipants();
        participantA = participants[0];
        participantB = participants[1];

        for (GamePlayer gamePlayer : participantA.getPlayers()) {
            if (!gamePlayer.isDisconnected()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    player.sendMessage(Locale.EVENT_ROUND_OPPONENT.format(player,
                            (participantB.getPlayers().size() == 1 ? "" : "s"),
                            participantB.getConjoinedNames()));

                    player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
                }
            }
        }

        for (GamePlayer gamePlayer : participantB.getPlayers()) {
            if (!gamePlayer.isDisconnected()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    player.sendMessage(Locale.EVENT_ROUND_OPPONENT.format(player,
                            (participantA.getPlayers().size() == 1 ? "" : "s"),
                            participantA.getConjoinedNames()));

                    player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
                }
            }
        }
    }

    @Override
    public void startRound() {
        game.sendMessage(Locale.EVENT_ROUND_START.format(game.getGameLogic().getRoundNumber(),
                participantA.getConjoinedNames(), participantB.getConjoinedNames()));

        game.sendSound(Sound.ORB_PICKUP, 1.0F, 15F);

        game.getGameMap().teleportFighters(game);

        for (GameParticipant<GamePlayer> participant : new GameParticipant[]{participantA, participantB}) {
            for (GamePlayer gamePlayer : participant.getPlayers()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    player.getInventory().setArmorContents(new ItemStack[4]);
                    player.getInventory().setContents(new ItemStack[36]);
                    player.updateInventory();
                }
            }
        }
    }

    @Override
    public boolean canStartRound() {
        return game.getRemainingParticipants() >= 2;
    }

    @Override
    public void endRound() {
        GameParticipant loser = getLosingParticipant();

        game.sendMessage(Locale.EVENT_ROUND_ELIMINATION.format(loser.getConjoinedNames(),
                loser.getPlayers().size() == 1 ? "was" : "were"));

        for (GameParticipant<GamePlayer> participant : new GameParticipant[]{participantA, participantB}) {
            for (GamePlayer gamePlayer : participant.getPlayers()) {
                Player player = gamePlayer.getPlayer();

                if (player != null) {
                    PlayerUtil.reset(player);
                    Practice.getInstance().getHotbar().giveHotbarItems(player);
                    player.teleport(game.getGameMap().getSpectatorPoint());
                }
            }
        }
    }

    @Override
    public boolean canEndRound() {
        return (participantA != null && participantA.isAllDead()) ||
                (participantB != null && participantB.isAllDead());
    }

    @Override
    public void onVote(Player player, EventGameMap gameMap) {
        if (game.getGameState() == EventGameState.WAITING_FOR_PLAYERS ||
                game.getGameState() == EventGameState.STARTING_EVENT) {
            EventGameMapVoteData voteData = game.getVotesData().get(gameMap);

            if (voteData != null) {
                if (voteData.hasVote(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You have already voted for that map!");
                } else {
                    for (EventGameMapVoteData otherVoteData : game.getVotesData().values()) {
                        if (otherVoteData.hasVote(player.getUniqueId())) {
                            otherVoteData.getPlayers().remove(player.getUniqueId());
                        }
                    }

                    voteData.addVote(player.getUniqueId());

                    game.sendMessage(Locale.EVENT_PLAYER_VOTE.format(player,
                            (player) + player.getName(),
                            gameMap.getMapName(),
                            voteData.getPlayers().size()
                    ));
                }
            } else {
                player.sendMessage(ChatColor.RED + "A map with that name does not exist.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "The event has already started.");
        }
    }

    @Override
    public void onJoin(Player player) {
        game.getParticipants().add(new GameParticipant<>(new GamePlayer(player.getUniqueId(), player.getName())));

        game.sendMessage(Locale.EVENT_PLAYER_JOIN.format(player, (player) + player.getName(),
                game.getParticipants().size(),
                game.getMaximumPlayers()));

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setState(ProfileState.EVENT);

        Practice.getInstance().getHotbar().giveHotbarItems(player);

        for (Map.Entry<EventGameMap, EventGameMapVoteData> entry : game.getVotesData().entrySet()) {
            ItemStack itemStack = Practice.getInstance().getHotbar().getItems().get(HotbarItem.MAP_SELECTION).clone();
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(itemMeta.getDisplayName().replace("%MAP%", entry.getKey().getMapName()));
            itemStack.setItemMeta(itemMeta);

            player.getInventory().addItem(itemStack);
        }

        player.updateInventory();
        player.teleport(game.getEvent().getLobbyLocation());

        VisibilityLogic.handle(player);

        for (GameParticipant<GamePlayer> gameParticipant : game.getParticipants()) {
            for (GamePlayer gamePlayer : gameParticipant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player bukkitPlayer = gamePlayer.getPlayer();

                    if (bukkitPlayer != null) {
                        VisibilityLogic.handle(bukkitPlayer, player);
                    }
                }
            }
        }
    }

    @Override
    public void onLeave(Player player) {
        if (isPlaying(player)) {
            onDeath(player, null);
        }

        Iterator<GameParticipant<GamePlayer>> iterator = game.getParticipants().iterator();

        while (iterator.hasNext()) {
            GameParticipant<GamePlayer> participant = iterator.next();

            if (participant.containsPlayer(player.getUniqueId())) {
                iterator.remove();

                for (GamePlayer gamePlayer : participant.getPlayers()) {
                    if (!gamePlayer.isDisconnected()) {
                        Player bukkitPlayer = gamePlayer.getPlayer();

                        if (bukkitPlayer != null) {
                            if (game.getGameState() == EventGameState.WAITING_FOR_PLAYERS ||
                                    game.getGameState() == EventGameState.STARTING_EVENT) {
                                game.sendMessage(Locale.EVENT_PLAYER_LEAVE.format(player,
                                        (bukkitPlayer) + bukkitPlayer.getName(),
                                        game.getRemainingPlayers(),
                                        game.getMaximumPlayers()
                                ));
                            }

                            Profile profile = Profile.getByUuid(bukkitPlayer.getUniqueId());
                            profile.setState(ProfileState.LOBBY);

                            Practice.getInstance().getHotbar().giveHotbarItems(bukkitPlayer);
                            VisibilityLogic.handle(bukkitPlayer, player);

                            Practice.getInstance().getEssentials().teleportToSpawn(bukkitPlayer);
                        }
                    }
                }
            }
        }

        VisibilityLogic.handle(player);
    }

    @Override
    public void onMove(Player player) {
        if (isPlaying(player)) {
            GamePlayer gamePlayer = game.getGamePlayer(player);

            if (gamePlayer != null) {
                if (BlockUtil.isOnLiquid(player.getLocation(), 0)) {
                    if (!gamePlayer.isDead()) {
                        onDeath(player, null);
                    }
                }
            }
        }
    }

    @Override
    public void onDeath(Player player, Player killer) {
        GamePlayer deadGamePlayer = game.getGamePlayer(player);

        if (deadGamePlayer != null) {
            deadGamePlayer.setDead(true);
        }

        if (participantA.isAllDead() || participantB.isAllDead()) {
            GameParticipant winner = getWinningParticipant();
            winner.reset();

            GameParticipant loser = getLosingParticipant();
            loser.setEliminated(true);

            if (canEndEvent()) {
                preEndEvent();
                game.setGameState(EventGameState.ENDING_EVENT);
                logicTask.setNextAction(3);
            } else if (canEndRound()) {
                game.setGameState(EventGameState.ENDING_ROUND);
                logicTask.setNextAction(1);
            }
        }
    }

    @Override
    public boolean isPlaying(Player player) {
        return (participantA != null && participantA.containsPlayer(player.getUniqueId())) ||
                (participantB != null && participantB.containsPlayer(player.getUniqueId()));
    }

    @Override
    public List<String> getScoreboardEntries() {
        List<String> lines = new ArrayList<>();
        lines.add("&cEvent &7(" + game.getEvent().getDisplayName() + ")");
        lines.add("&4* &rPlayers: &7" + game.getRemainingPlayers() + "/" + game.getMaximumPlayers());

        if (game.getGameState() == EventGameState.STARTING_ROUND ||
                game.getGameState() == EventGameState.PLAYING_ROUND ||
                game.getGameState() == EventGameState.ENDING_ROUND) {
            lines.add("&4* &rRound: &7" + roundNumber);
        }

        switch (game.getGameState()) {
            case WAITING_FOR_PLAYERS: {
                lines.add("&4* &rWaiting for players");
            }
            break;
            case STARTING_EVENT: {
                lines.add("&4* &rStarting In: &7" + game.getGameLogic().getGameLogicTask().getNextActionTime());
            }
            break;
            case PLAYING_ROUND: {
                lines.add(CC.SB_BAR);

                if (participantA instanceof TeamGameParticipant) {
                    lines.add("&4" + participantA.getConjoinedNames());
                    lines.add("&cvs");
                    lines.add("&4" + participantB.getConjoinedNames());
                } else {
                    lines.add("&4" + participantA.getConjoinedNames() + " &cvs &4" + participantB.getConjoinedNames());
                }
            }
            break;
            case STARTING_ROUND:
            case ENDING_ROUND: {
                lines.add(CC.SB_BAR);
                lines.add("&cWaiting...");
            }
            break;
            case ENDING_EVENT: {
                if (winningParticipant != null) {
                    lines.add("&4* &rRounds: &7" + roundNumber);
                    lines.add(CC.SB_BAR);
                    lines.add("&cWinner: &4" + winningParticipant.getConjoinedNames());
                }
            }
            break;
        }

        if (game.getGameState() == EventGameState.WAITING_FOR_PLAYERS ||
                game.getGameState() == EventGameState.STARTING_EVENT) {
            lines.add(CC.SB_BAR);
            lines.add("&cMap Votes");

            game.getVotesData().forEach((map, voteData) -> {
                lines.add("&4* &r" + map.getMapName() + " &7(" +
                        voteData.getPlayers().size() + " votes)");
            });
        }

        return lines;
    }

    private GameParticipant<GamePlayer>[] findParticipants() {
        List<GameParticipant<GamePlayer>> participants = game.getParticipants()
                .stream()
                .filter(gameParticipant -> !gameParticipant.isEliminated())
                .sorted(Comparator.comparingInt(GameParticipant::getRoundWins))
                .collect(Collectors.toList());

        if (participants.size() <= 1) {
            return null;
        }

        GameParticipant<GamePlayer>[] array = new GameParticipant[]{
                participants.get(0),
                participants.get(1)
        };

        int grabFromIndex = 2;

        if (array[0].equals(participantA) && participants.size() > grabFromIndex) {
            array[0] = participants.get(grabFromIndex++);
        }

        if (array[0].equals(participantB) && participants.size() > grabFromIndex) {
            array[0] = participants.get(grabFromIndex++);
        }

        if (array[1].equals(participantA) && participants.size() > grabFromIndex) {
            array[1] = participants.get(grabFromIndex++);
        }

        if (array[1].equals(participantB) && participants.size() > grabFromIndex) {
            array[1] = participants.get(grabFromIndex++);
        }

        return array;
    }

    private GameParticipant getWinningParticipant() {
        if (participantA.isAllDead()) {
            return participantB;
        } else {
            return participantA;
        }
    }

    private GameParticipant getLosingParticipant() {
        if (participantA.isAllDead()) {
            return participantA;
        } else {
            return participantB;
        }
    }
}
