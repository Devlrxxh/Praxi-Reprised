package me.lrxh.practice.scoreboard;

import me.lrxh.practice.Practice;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.MatchState;
import me.lrxh.practice.match.impl.BasicFreeForAllMatch;
import me.lrxh.practice.match.impl.BasicTeamMatch;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.queue.QueueProfile;
import me.lrxh.practice.util.PlaceholderUtil;
import me.lrxh.practice.util.PlayerUtil;
import me.lrxh.practice.util.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardAdapter implements AssembleAdapter {

    public String getTitle(Player player) {
        ArrayList<String> list = new ArrayList<>();
        list.add(getAnimatedText());
        return PlaceholderUtil.format(list, player).toString().replace("[", "").replace("]", "");
    }

    public List<String> getLines(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getState() == ProfileState.LOBBY) {
            if (profile.getParty() != null) {
                return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("IN-PARTY.LOBBY")), player);
            }
            if (Practice.getInstance().isReplay()) {
                if (PlayerUtil.inReplay(player)) {
                    return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("REPLAYING")), player);
                }
            }
            return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("LOBBY")), player);
        }

        if (profile.getState() == ProfileState.SPECTATING) {
            return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("SPECTATING")), player);
        }

        if (profile.getState() == ProfileState.QUEUEING) {
            QueueProfile queueProfile = profile.getQueueProfile();

            if (queueProfile.getQueue().isRanked()) {
                return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("QUEUE.RANKED")), player);
            }
            return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("QUEUE.UNRANKED")), player);
        }

        if (profile.getMatch() != null) {
            Match match = profile.getMatch();
            if (match instanceof BasicTeamMatch && profile.getParty() != null) {
                return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("IN-PARTY.IN-SPLIT-MATCH")), player);
            }
            if (match instanceof BasicFreeForAllMatch) {
                return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("IN-PARTY.IN-FFA-MATCH")), player);
            }

            if (match.getState().equals(MatchState.STARTING_ROUND)) {
                return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("MATCH.STARTING")), player);
            }
            if (match.getState().equals(MatchState.ENDING_MATCH)) {
                return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("MATCH.ENDING")), player);
            }
            if (match.getState().equals(MatchState.PLAYING_ROUND)) {
                if (match.getKit().getGameRules().isBoxing()) {
                    return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("MATCH.IN-MATCH-BOXING")), player);
                }
                if (match.getKit().getGameRules().isBedwars()) {
                    return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("MATCH.IN-MATCH-BEDWARS")), player);
                }
                return PlaceholderUtil.format(new ArrayList<>(Practice.getInstance().getScoreboardConfig().getStringList("MATCH.IN-MATCH")), player);
            }
        }

        return null;
    }

    private String getAnimatedText() {
        int index = (int) ((System.currentTimeMillis() / Practice.getInstance().getScoreboardConfig().getInteger("UPDATE-INTERVAL"))
                % Practice.getInstance().getScoreboardConfig().getStringList("TITLE").size());
        return Practice.getInstance().getScoreboardConfig().getStringList("TITLE").get(index);
    }
}
