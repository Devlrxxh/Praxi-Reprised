package me.funky.praxi.event.game;

import me.funky.praxi.event.game.map.EventGameMap;
import java.util.List;
import org.bukkit.entity.Player;

public interface EventGameLogic {

	EventGameLogicTask getGameLogicTask();

	void startEvent();

	boolean canStartEvent();

	void preEndEvent();

	void endEvent();

	boolean canEndEvent();

	void cancelEvent();

	void preStartRound();

	void startRound();

	boolean canStartRound();

	void endRound();

	boolean canEndRound();

	void onVote(Player player, EventGameMap gameMap);

	void onJoin(Player player);

	void onLeave(Player player);

	void onMove(Player player);

	void onDeath(Player player, Player killer);

	boolean isPlaying(Player player);

	List<String> getScoreboardEntries();

	int getRoundNumber();

}
