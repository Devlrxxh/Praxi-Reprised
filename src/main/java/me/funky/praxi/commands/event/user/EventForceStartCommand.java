package me.funky.praxi.commands.event.user;

import me.funky.praxi.event.game.EventGame;
import me.funky.praxi.event.game.EventGameState;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandMeta(label = "event forcestart", permission = "praxi.admin.event")
public class EventForceStartCommand {

    public void execute(Player player) {
        if (EventGame.getActiveGame() != null) {
            EventGame game = EventGame.getActiveGame();

            if (game.getGameState() == EventGameState.WAITING_FOR_PLAYERS ||
                    game.getGameState() == EventGameState.STARTING_EVENT) {
                game.getGameLogic().startEvent();
                game.getGameLogic().preStartRound();
                game.setGameState(EventGameState.STARTING_ROUND);
                game.getGameLogic().getGameLogicTask().setNextAction(4);
            } else {
                player.sendMessage(ChatColor.RED + "The event has already started.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "There is no active event.");
        }
    }

}
