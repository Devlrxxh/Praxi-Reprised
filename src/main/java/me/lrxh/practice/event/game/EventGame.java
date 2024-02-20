package me.lrxh.practice.event.game;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.event.Event;
import me.lrxh.practice.event.game.map.EventGameMap;
import me.lrxh.practice.event.game.map.vote.EventGameMapVoteData;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.participant.GamePlayer;
import me.lrxh.practice.util.*;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventGame {

    @Getter
    @Setter
    private static EventGame activeGame;
    @Getter
    @Setter
    private static Cooldown cooldown = new Cooldown(0);
    @Getter
    private final Map<EventGameMap, EventGameMapVoteData> votesData;
    @Getter
    private final Event event;
    @Getter
    private final GamePlayer gameHost;
    @Getter
    private final List<GameParticipant<GamePlayer>> participants;
    @Getter
    private final int maximumPlayers;
    @Getter
    @Setter
    private EventGameState gameState;
    @Getter
    private EventGameLogic gameLogic;
    @Getter
    @Setter
    private EventGameMap gameMap;

    public EventGame(Event event, Player player, int maximumPlayers) {
        this.event = event;
        this.gameHost = new GamePlayer(player.getUniqueId(), player.getName());
        this.participants = new ArrayList<>();
        this.votesData = new HashMap<>();
        this.maximumPlayers = maximumPlayers;

        activeGame = this;
    }


    public int getRemainingParticipants() {
        if (gameState == EventGameState.WAITING_FOR_PLAYERS) {
            return participants.size();
        }

        int i = 0;

        for (GameParticipant participant : participants) {
            if (!participant.isEliminated()) {
                i++;
            }
        }

        return i;
    }

    public int getRemainingPlayers() {
        if (gameState == EventGameState.WAITING_FOR_PLAYERS) {
            return participants.size();
        }

        int i = 0;

        for (GameParticipant<GamePlayer> participant : participants) {
            if (!participant.isEliminated()) {
                i += participant.getPlayers().size();
            }
        }

        return i;
    }

    public GamePlayer getGamePlayer(Player player) {
        for (GameParticipant<GamePlayer> participant : participants) {
            if (participant.containsPlayer(player.getUniqueId())) {
                for (GamePlayer gamePlayer : participant.getPlayers()) {
                    if (gamePlayer.getUuid().equals(player.getUniqueId())) {
                        return gamePlayer;
                    }
                }
            }
        }

        return null;
    }

    public void sendMessage(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        for (GameParticipant<GamePlayer> participant : participants) {
            for (GamePlayer gamePlayer : participant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player player = gamePlayer.getPlayer();

                    if (player != null) {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(CC.translate(message));
                        player.sendMessage(PlaceholderUtil.format(list, player).toString().replace("[", "").replace("]", ""));
                    }
                }
            }
        }
    }

    public void sendSound(Sound sound, float volume, float pitch) {
        for (GameParticipant<GamePlayer> participant : participants) {
            for (GamePlayer gamePlayer : participant.getPlayers()) {
                if (!gamePlayer.isDisconnected()) {
                    Player player = gamePlayer.getPlayer();

                    if (player != null) {
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    }
                }
            }
        }
    }

    public void broadcastJoinMessage() {
        Player hostPlayer = gameHost.getPlayer();
        List<BaseComponent[]> compiledComponents = new ArrayList<>();
        List<String> lines = Locale.EVENT_JOIN_BROADCAST.formatLines(event.getDisplayName(),
                (hostPlayer == null ? "" : (hostPlayer)) + gameHost.getUsername());

        for (String line : lines) {
            compiledComponents.add(new ChatComponentBuilder("")
                    .parse(line)
                    .attachToEachPart(ChatHelper.hover(Locale.EVENT_JOIN_HOVER.format()))
                    .attachToEachPart(ChatHelper.click("/event join"))
                    .create());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (BaseComponent[] components : compiledComponents) {
                player.spigot().sendMessage(components);
            }
        }
    }

    public void start() {
        gameState = EventGameState.WAITING_FOR_PLAYERS;
        gameLogic = event.start(this);
    }
}
