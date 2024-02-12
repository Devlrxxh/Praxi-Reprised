package me.funky.praxi.profile.hotbar;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.profile.ProfileState;

import java.util.regex.Pattern;

public enum HotbarItem {

    QUEUE_JOIN_RANKED(null),
    QUEUE_JOIN_UNRANKED(null),
    QUEUE_LEAVE(null),
    PARTY_EVENTS(null),
    SETTINGS("settings"),
    PARTY_CREATE("party create"),
    PARTY_DISBAND("party disband"),
    PARTY_LEAVE("party leave"),
    PARTY_INFORMATION("party info"),
    OTHER_PARTIES(null),
    KIT_EDITOR(null),
    SPECTATE_STOP("spec leave"),
    VIEW_INVENTORY(null),
    EVENT_JOIN("event join"),
    EVENT_LEAVE("event leave"),
    MAP_SELECTION(null),
    REMATCH_REQUEST("rematch"),
    REMATCH_ACCEPT("rematch"),
    KIT_SELECTION(null);

    @Getter
    private final String command;
    @Getter
    @Setter
    private Pattern pattern;
    @Getter
    @Setter
    private ProfileState state;
    @Getter
    @Setter
    private int slot;
    @Getter
    @Setter
    private boolean party;

    HotbarItem(String command) {
        this.command = command;
    }

}
