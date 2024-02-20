package me.lrxh.practice.participant;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Data
public class GamePlayer {

    private final UUID uuid;
    private final String username;
    private boolean disconnected;
    private boolean dead;

    public GamePlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

}
