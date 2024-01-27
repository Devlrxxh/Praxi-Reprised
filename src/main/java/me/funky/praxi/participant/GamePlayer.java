package me.funky.praxi.participant;

import java.util.UUID;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
