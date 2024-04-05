package me.lrxh.practice.match.task;

import lombok.Setter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.MatchState;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.participant.GamePlayer;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchLogicTask extends BukkitRunnable {

    private final Match match;
    @Setter
    private int nextAction = 6;

    public MatchLogicTask(Match match) {
        this.match = match;
    }


    @Override
    public void run() {
        if (!Practice.getInstance().getCache().getMatches().contains(match)) {
            cancel();
            return;
        }
        nextAction--;
        if (match.getState() == MatchState.STARTING_ROUND) {
            if (match.getKit().getGameRules().isSumo() || match.getKit().getGameRules().isBedwars()) {
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

                if (match.getKit().getGameRules().isSumo() || match.getKit().getGameRules().isBedwars()) {
                    for (GameParticipant<MatchGamePlayer> gameParticipant : match.getParticipants()) {
                        for (GamePlayer gamePlayer : gameParticipant.getPlayers()) {
                            PlayerUtil.allowMovement(gamePlayer.getPlayer());
                        }
                    }
                }
                match.sendSound(Sound.FIREWORK_BLAST, 1.0F, 1.0F);
            } else {
                match.sendMessage(Locale.MATCH_START_TIMER.format(nextAction, nextAction == 1 ? "" : "s"));
                match.sendTitle(CC.translate("&e" + nextAction), "", 20);
                match.sendSound(Sound.CLICK, 1.0F, 1.0F);
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
