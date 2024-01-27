package me.funky.praxi.match.participant;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.participant.GamePlayer;

import java.util.UUID;

@Getter
public class MatchGamePlayer extends GamePlayer {

    private final int elo;
    @Setter
    private int eloMod;
    private int hits;
    private int longestCombo;
    private int combo;
    private int potionsThrown;
    private int potionsMissed;

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
