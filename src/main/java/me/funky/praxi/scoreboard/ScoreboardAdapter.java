package me.funky.praxi.scoreboard;

import com.bizarrealex.aether.scoreboard.Board;
import com.bizarrealex.aether.scoreboard.BoardAdapter;
import com.bizarrealex.aether.scoreboard.cooldown.BoardCooldown;
import me.funky.praxi.event.game.EventGame;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.queue.QueueProfile;
import java.util.ArrayList;
import java.util.List;
import me.funky.praxi.Praxi;
import me.funky.praxi.party.Party;
import java.util.Set;

import me.funky.praxi.util.CC;
import me.funky.praxi.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardAdapter implements BoardAdapter {

	private int inQueues;
	private int inFights;

	public ScoreboardAdapter() {
		new BukkitRunnable() {
			@Override
			public void run() {
				int inQueues = 0;
				int inFights = 0;

				for (Player player : Bukkit.getOnlinePlayers()) {
					Profile profile = Profile.getByUuid(player.getUniqueId());

					if (profile != null) {
						if (profile.getState() == ProfileState.QUEUEING) {
							inQueues++;
						} else if (profile.getState() == ProfileState.FIGHTING || profile.getState() == ProfileState.EVENT) {
							inFights++;
						}
					}
				}

				ScoreboardAdapter.this.inQueues = inQueues;
				ScoreboardAdapter.this.inFights = inFights;
			}
		}.runTaskTimerAsynchronously(Praxi.getInstance(), 2L, 2L);
	}

	@Override
	public String getTitle(Player player) {
		return Praxi.getInstance().getMainConfig().getString("SCOREBOARD.TITLE");
	}

	@Override
	public List<String> getScoreboard(Player player, Board board, Set<BoardCooldown> cooldowns) {
		Profile profile = Profile.getByUuid(player.getUniqueId());

		if (!profile.getOptions().showScoreboard()) {
			return null;
		}

		List<String> lines = new ArrayList<>();

		if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.QUEUEING) {
			lines.add("&cOnline: &7" + Bukkit.getOnlinePlayers().size());
			lines.add("&cIn Fights: &7" + inFights);
			lines.add("&cIn Queues: &7" + inQueues);

			if (EventGame.getActiveGame() == null && !EventGame.getCooldown().hasExpired()) {
				lines.add("&cEvent Cooldown: &7" + TimeUtil.millisToTimer(EventGame.getCooldown().getRemaining()));
			}
		}

		if (profile.getState() == ProfileState.LOBBY) {
			if (profile.getParty() != null) {
				lines.add("&cYour Party");

				int added = 0;
				Party party = profile.getParty();

				for (Player otherPlayer : party.getListOfPlayers()) {
					added++;

					lines.add(" &7" + (party.getLeader().equals(otherPlayer) ? "*" : "-") + " &r" +
					          otherPlayer.getName());

					if (added >= 4) {
						break;
					}
				}
			}
		} else if (profile.getState() == ProfileState.QUEUEING) {
			QueueProfile queueProfile = profile.getQueueProfile();

			lines.add(CC.SB_BAR);
			lines.add("&a&oSearching for a match...");
			lines.add(" ");
			lines.add("&c" + queueProfile.getQueue().getQueueName());
			lines.add("&cElapsed: &7" + TimeUtil.millisToTimer(queueProfile.getPassed()));

			if (queueProfile.getQueue().isRanked()) {
				lines.add("&cELO Range: &7" + queueProfile.getMinRange() + " -> " + queueProfile.getMaxRange());
			}
		} else if (profile.getState() == ProfileState.FIGHTING || profile.getState() == ProfileState.SPECTATING) {
			lines.addAll(profile.getMatch().getScoreboardLines(player));
		} else if (profile.getState() == ProfileState.EVENT) {
			lines.addAll(EventGame.getActiveGame().getGameLogic().getScoreboardEntries());
		}

		lines.add(0, CC.SB_BAR);
		lines.add(CC.SB_BAR);

		return lines;
	}

	@Override
	public void onScoreboardCreate(Player p0, Scoreboard p1) {

	}

}
