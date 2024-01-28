package me.funky.praxi.match.task;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.Locale;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.MatchState;
import me.funky.praxi.match.participant.MatchGamePlayer;
import me.funky.praxi.participant.GameParticipant;
import me.funky.praxi.participant.GamePlayer;
import me.funky.praxi.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchLogicTask extends BukkitRunnable {

    private final Match match;
    private int totalTicked;
    @Getter
    @Setter
    private int nextAction;

    public MatchLogicTask(Match match) {
        this.match = match;

        if (match.getKit().getGameRules().isSumo()) {
            nextAction = 4;
        } else {
            nextAction = 6;
        }
    }

    @Override
    public void run() {
        totalTicked++;
        nextAction--;
        // Deny movement if the kit is sumo


        if (match.getState() == MatchState.STARTING_ROUND) {
        if (match.getKit().getGameRules().isSumo()) {
            for (GameParticipant<MatchGamePlayer> gameParticipant : match.getParticipants()) {
                for (GamePlayer gamePlayer : gameParticipant.getPlayers()) {
                    PlayerUtil.denyMovement(gamePlayer.getPlayer());
                }
            }
        }
            if (nextAction == 0) {
                match.onRoundStart();
                match.setState(MatchState.PLAYING_ROUND);
                match.sendMessage(Locale.MATCH_STARTED.format());
                match.sendSound(Sound.ORB_PICKUP, 1.0F, 1.0F);
                if (match.getKit().getGameRules().isSumo()) {
                    for (GameParticipant<MatchGamePlayer> gameParticipant : match.getParticipants()) {
                        for (GamePlayer gamePlayer : gameParticipant.getPlayers()) {
                            PlayerUtil.allowMovement(gamePlayer.getPlayer());
                        }
                    }
                }
            } else {
                match.sendMessage(Locale.MATCH_START_TIMER.format(nextAction, nextAction == 1 ? "" : "s"));
                match.sendSound(Sound.ORB_PICKUP, 1.0F, 15F);
            }
        } else if (match.getState() == MatchState.ENDING_ROUND) {
            if (nextAction == 0) {
                if (match.canStartRound()) {
                    match.onRoundStart();
                }
            }
        } else if (match.getState() == MatchState.ENDING_MATCH) {
            if (nextAction == 0) {
                match.end();
            }
        }
    }

}
