package me.funky.praxi.util;

import lombok.experimental.UtilityClass;
import me.funky.praxi.Praxi;
import me.funky.praxi.match.Match;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.queue.QueueProfile;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class PlaceholderUtil {

    public static List<String> format(List<String> lines, Player player) {
        List<String> formattedLines = new ArrayList<>();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        QueueProfile queueProfile = profile.getQueueProfile();
        for (String line : lines) {
            line = line.replaceAll("<online>", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()));
            line = line.replaceAll("<queued>", String.valueOf(Praxi.getInstance().getCache().getPlayers().size()));
            line = line.replaceAll("<in-match>", String.valueOf(Praxi.getInstance().getCache().getMatches().size()));
            line = line.replaceAll("<player>", player.getName());
            line = line.replaceAll("<ping>", String.valueOf((((CraftPlayer) player).getHandle()).ping));

            if (profile.getState() == ProfileState.QUEUEING) {
                line = line.replaceAll("<kit>", queueProfile.getQueue().getKit().getName());
                line = line.replaceAll("<time>", TimeUtil.millisToTimer(queueProfile.getPassed()));
                line = line.replaceAll("<minElo>", String.valueOf(queueProfile.getMinRange()));
                line = line.replaceAll("<maxElo>", String.valueOf(queueProfile.getMaxRange()));
            }

            if (profile.getParty() != null) {
                line = line.replaceAll("<leader>", profile.getParty().getLeader().getName());
                line = line.replaceAll("<party-size>", String.valueOf(profile.getParty().getListOfPlayers().size()));
            }

            if (profile.getState() == ProfileState.FIGHTING) {
                Match match = profile.getMatch();
                line = line.replaceAll("<opponent>", match.getOpponent(player).getName());
                line = line.replaceAll("<opponent-ping>", String.valueOf((((CraftPlayer) match.getOpponent(player)).getHandle()).ping));
            }
                formattedLines.add(line);
        }
        return formattedLines;
    }
}
