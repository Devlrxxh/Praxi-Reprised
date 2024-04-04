package me.lrxh.practice.match.participant;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.kit.KitLoadout;
import me.lrxh.practice.participant.GamePlayer;

import java.util.UUID;

@Getter
@Setter
public class MatchGamePlayer extends GamePlayer {

    private final int elo;
    private int eloMod;
    private int hits;
    private int longestCombo;
    private int combo;
    private int potionsThrown;
    private int potionsMissed;
    private boolean respawned = false;
    private KitLoadout kitLoadout = null;

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
