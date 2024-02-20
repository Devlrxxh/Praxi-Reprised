package me.lrxh.practice.event;

import me.lrxh.practice.Practice;
import me.lrxh.practice.event.game.EventGame;
import me.lrxh.practice.event.game.EventGameLogic;
import me.lrxh.practice.event.impl.sumo.SumoEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface Event {

    List<Event> events = new ArrayList<>();

    static void init() {
        events.add(new SumoEvent());

        for (Event event : events) {
            for (Listener listener : event.getListeners()) {
                Practice.getInstance().getServer().getPluginManager().registerEvents(listener, Practice.getInstance());
            }

            for (Object command : event.getCommands()) {
                Practice.getInstance().getHoncho().registerCommand(command);
            }
        }
    }

    static Event getByName(String mapName) {
        for (Event event : events) {
            if (event.getDisplayName().equalsIgnoreCase(mapName)) {
                return event;
            }
        }

        return null;
    }

    static <T extends Event> T getEvent(Class<? extends Event> clazz) {
        for (Event event : events) {
            if (event.getClass() == clazz) {
                return (T) clazz.cast(event);
            }
        }

        return null;
    }

    String getDisplayName();

    String getDisplayName(EventGame game);

    List<String> getDescription();

    Location getLobbyLocation();

    void setLobbyLocation(Location location);

    ItemStack getIcon();

    boolean canHost(Player player);

    List<String> getAllowedMaps();

    List<Listener> getListeners();

    default List<Object> getCommands() {
        return new ArrayList<>();
    }

    EventGameLogic start(EventGame game);

    void save();

}
