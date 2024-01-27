package me.funky.praxi.match.participant;

import me.funky.praxi.participant.GamePlayer;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class MatchGamePlayer extends GamePlayer {

	@Getter private final int elo;
	@Getter @Setter private int eloMod;
	@Getter private int hits;
	@Getter private int longestCombo;
	@Getter private int combo;
	@Getter private int potionsThrown;
	@Getter private int potionsMissed;

	public MatchGamePlayer(UUID uuid, String username) {
		this(uuid, username, 0);
	}

	public MatchGamePlayer(UUID uuid, String username, int elo) {
		super(uuid, username);

		this.elo = elo;
	}

	public void incrementPotionsThrown() {
		potionsThrown++;
	}

	public void incrementPotionsMissed() {
		potionsMissed++;
	}

	public void handleHit() {
		hits++;
		combo++;

		if (combo > longestCombo) {
			longestCombo = combo;
		}
	}

	public void resetCombo() {
		combo = 0;
	}

}
