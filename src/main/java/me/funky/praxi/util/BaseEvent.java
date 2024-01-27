package me.funky.praxi.util;

import me.funky.praxi.Praxi;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BaseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void call() {
        Praxi.getInstance().getServer().getPluginManager().callEvent(this);
    }

}
