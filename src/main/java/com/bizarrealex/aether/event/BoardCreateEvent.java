package com.bizarrealex.aether.event;

import com.bizarrealex.aether.scoreboard.Board;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BoardCreateEvent extends Event {
    private static final HandlerList HANDLERS;
    private final Board board;
    private final Player player;
    
    public BoardCreateEvent(final Board board, final Player player) {
        this.board = board;
        this.player = player;
    }
    
    public static HandlerList getHandlerList() {
        return BoardCreateEvent.HANDLERS;
    }
    
    public HandlerList getHandlers() {
        return BoardCreateEvent.HANDLERS;
    }
    
    public Board getBoard() {
        return this.board;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    static {
        HANDLERS = new HandlerList();
    }
}
