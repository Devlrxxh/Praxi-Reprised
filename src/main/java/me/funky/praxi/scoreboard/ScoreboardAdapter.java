package me.funky.praxi.scoreboard;

import me.funky.praxi.Praxi;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.queue.QueueProfile;
import me.funky.praxi.util.PlaceholderUtil;
import me.funky.praxi.util.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardAdapter implements AssembleAdapter {
    public String getTitle(Player player) {
        return Praxi.getInstance().getScoreboardConfig().getString("TITLE");
    }

    public List<String> getLines(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getState() == ProfileState.LOBBY) {

            if (profile.getParty() != null) {
                return PlaceholderUtil.format(new ArrayList<>(Praxi.getInstance().getScoreboardConfig().getStringList("IN-PARTY")), player);
            }
            return PlaceholderUtil.format(new ArrayList<>(Praxi.getInstance().getScoreboardConfig().getStringList("LOBBY")), player);
        }

        if (profile.getState() == ProfileState.QUEUEING) {
            QueueProfile queueProfile = profile.getQueueProfile();

            if (queueProfile.getQueue().isRanked()) {
                return PlaceholderUtil.format(new ArrayList<>(Praxi.getInstance().getScoreboardConfig().getStringList("QUEUE.RANKED")), player);
            }
            return PlaceholderUtil.format(new ArrayList<>(Praxi.getInstance().getScoreboardConfig().getStringList("QUEUE.UNRANKED")), player);
        }


        return null;
    }
}
