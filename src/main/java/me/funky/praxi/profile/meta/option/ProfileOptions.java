package me.funky.praxi.profile.meta.option;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.funky.praxi.profile.KillEffects;

@Setter
@Getter
@Accessors(fluent = true)
public class ProfileOptions {
    private boolean showScoreboard = true;
    private boolean receiveDuelRequests = true;
    private boolean allowSpectators = true;
    private boolean showPlayers = false;
    private KillEffects killEffect = KillEffects.NONE;
}
