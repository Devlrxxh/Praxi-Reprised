package me.lrxh.practice.match.task;

import lombok.Setter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.MatchState;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.participant.GamePlayer;
import me.lrxh.practice.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchLogicTask extends BukkitRunnable {

    private final Match match;
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
        nextAction--;
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
                match.sendMessage(" ");
                match.sendMessage(Locale.MATCH_WARNING.format());
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
                if (match.getKit().getGameRules().isBoxing()) {
                    for (GameParticipant<MatchGamePlayer> players : match.getParticipants()) {
                        players.addSpeed();
                    }
                }
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
