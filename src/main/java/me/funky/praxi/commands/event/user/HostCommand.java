package me.funky.praxi.commands.event.user;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.funky.praxi.Praxi;
import me.funky.praxi.event.Event;
import me.funky.praxi.event.game.EventGame;
import me.funky.praxi.event.game.EventGameState;
import me.funky.praxi.event.game.map.EventGameMap;
import me.funky.praxi.event.game.menu.EventHostMenu;
import me.funky.praxi.event.impl.sumo.SumoEvent;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.Cooldown;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("host|events|event")
@Description("Host Events.")
public class HostCommand extends BaseCommand {
    private final String[][] HELP = new String[][]{
            new String[]{"/event forcestart", "Force start the active event"},
            new String[]{"/event cancel", "Cancel the active event"},
            new String[]{"/event clearcd", "Clear the event cooldown"},
            new String[]{"/event set lobby <event>", "Set lobby location"},
            new String[]{"/event add map <event> <map>", "Allow a map to be played"},
            new String[]{"/event remove map <event> <map>", "Deny a map to be played"},
            new String[]{"/event map create <name>", "Create a map"},
            new String[]{"/event map delete <name>", "Delete a map"},
            new String[]{"/event map set spawn <name>", "Set a spawn point"},
            new String[]{"/event map status <map>", "Check the status of a map"}
    };

    @Default
    public void open(Player player) {
        new EventHostMenu().openMenu(player);
    }

    @Subcommand("help")
    @CommandPermission("praxi.admin.event")
    public void help(Player player) {
        for (String line : Praxi.getInstance().getMainConfig().getStringList("EVENT.HELP")) {
            player.sendMessage(CC.translate(line));
        }
    }

    @Subcommand("cancel")
    @CommandPermission("praxi.admin.event")
    public void cancel(Player player) {
        if (EventGame.getActiveGame() != null) {
            EventGame.getActiveGame().getGameLogic().cancelEvent();
        } else {
            player.sendMessage(ChatColor.RED + "There is no active event.");

        }
    }

    @Subcommand("clearcooldown")
    @CommandPermission("praxi.admin.event")
    public void clearcooldown(Player player) {
        EventGame.setCooldown(new Cooldown(0));
        player.sendMessage(ChatColor.GREEN + "You cleared the event cooldown.");
    }

    @Subcommand("forcestart")
    @CommandPermission("praxi.admin.event")
    public void forcestart(Player player) {
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

    @Subcommand("info")
    @CommandPermission("praxi.admin.event")
    public void info(Player player) {
        if (EventGame.getActiveGame() == null) {
            player.sendMessage(CC.RED + "There is no active event.");
            return;
        }

        EventGame game = EventGame.getActiveGame();

        player.sendMessage(CC.GOLD + CC.BOLD + "Event Information");
        player.sendMessage(CC.BLUE + "State: " + CC.YELLOW + game.getGameState().getReadable());
        player.sendMessage(CC.BLUE + "Players: " + CC.YELLOW + game.getRemainingPlayers() +
                "/" + game.getMaximumPlayers());

        if (game.getEvent() instanceof SumoEvent) {
            player.sendMessage(CC.BLUE + "Round: " + CC.YELLOW + game.getGameLogic().getRoundNumber());
        }
    }

    @Subcommand("join")
    public void join(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() != null) {
            player.sendMessage(CC.RED + "You cannot join the event while in a party.");
            return;
        }

        if (profile.isBusy()) {
            player.sendMessage(CC.RED + "You must be in the lobby to join the event.");
        } else {
            EventGame game = EventGame.getActiveGame();

            if (game != null) {
                if (game.getGameState() == EventGameState.WAITING_FOR_PLAYERS ||
                        game.getGameState() == EventGameState.STARTING_EVENT) {
                    if (game.getParticipants().size() < game.getMaximumPlayers()) {
                        game.getGameLogic().onJoin(player);
                    } else {
                        player.sendMessage(CC.RED + "The event is full.");
                    }
                } else {
                    player.sendMessage(CC.RED + "The event has already started.");
                }
            } else {
                player.sendMessage(CC.RED + "There is no active event.");
            }
        }
    }

    @Subcommand("leave")
    public void leave(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getState() == ProfileState.EVENT) {
            EventGame.getActiveGame().getGameLogic().onLeave(player);
        } else {
            player.sendMessage(CC.RED + "You are not in an event.");
        }
    }

    @Subcommand("add map")
    @CommandPermission("praxi.admin.event")
    @CommandCompletion("@events @maps")
    @Syntax("<event> <map>")
    public void addmap(Player player, String eventString, String map) {
        EventGameMap gameMap = EventGameMap.getByName(map);
        Event event = Event.getByName(eventString);

        if (event == null) {
            player.sendMessage(CC.RED + "An event type by that name does not exist.");
            player.sendMessage(CC.RED + "Types: sumo");
            return;
        }

        if (gameMap == null) {
            player.sendMessage(CC.RED + "A map with that name does not exist.");
            return;
        }

        if (!event.getAllowedMaps().contains(gameMap.getMapName())) {
            event.getAllowedMaps().add(gameMap.getMapName());
            event.save();

            player.sendMessage(CC.GOLD + "You successfully added the \"" + CC.GREEN + gameMap.getMapName() +
                    CC.GOLD + "\" map from the \"" + CC.GREEN + gameMap.getMapName() + CC.GOLD +
                    "\" event.");
        }
    }

    @Subcommand("admin")
    @CommandPermission("praxi.admin.event")
    public void admin(Player player) {
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.GOLD + "Event Admin");

        for (String[] command : HELP) {
            player.sendMessage(CC.BLUE + command[0] + CC.GRAY + " - " + CC.WHITE + command[1]);
        }

        player.sendMessage(CC.CHAT_BAR);
    }

    @Subcommand("remove map")
    @CommandPermission("praxi.admin.event")
    @CommandCompletion("@events @maps")
    @Syntax("<event> <map>")
    public void removemap(Player player, String eventString, String map) {
        EventGameMap gameMap = EventGameMap.getByName(map);
        Event event = Event.getByName(eventString);

        if (event == null) {
            player.sendMessage(CC.RED + "An event type by that name does not exist.");
            player.sendMessage(CC.RED + "Types: sumo");
            return;
        }

        if (gameMap == null) {
            player.sendMessage(CC.RED + "A map with that name does not exist.");
            return;
        }

        if (event.getAllowedMaps().remove(gameMap.getMapName())) {
            event.save();

            player.sendMessage(CC.GREEN + "You successfully removed the \"" + gameMap.getMapName() +
                    "\" map from the \"" + event.getDisplayName() + "\" event.");
        }
    }

    @Subcommand("remove map")
    @CommandPermission("praxi.admin.event")
    @CommandCompletion("@event")
    @Syntax("<event>")
    public void removemap(Player player, String eventString) {
        Event event = Event.getByName(eventString);

        if (event != null) {
            event.setLobbyLocation(player.getLocation());
            event.save();

            player.sendMessage(ChatColor.GOLD + "You updated the " + ChatColor.GREEN + event.getDisplayName() +
                    ChatColor.GOLD + " Event's lobby location.");
        } else {
            player.sendMessage(ChatColor.RED + "An event with that name does not exist.");
        }
    }
}
