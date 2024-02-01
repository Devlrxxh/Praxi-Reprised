package me.funky.praxi.profile.meta.option;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(fluent = true)
public class ProfileOptions {
    private boolean showScoreboard = true;
    private boolean receiveDuelRequests = true;
    private boolean allowSpectators = true;
}
