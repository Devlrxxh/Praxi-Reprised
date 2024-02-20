package me.funky.praxi.profile.meta.option;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.funky.praxi.Praxi;
import me.funky.praxi.profile.KillEffects;
import me.funky.praxi.setting.Colors;

@Setter
@Getter
@Accessors(fluent = true)
public class ProfileOptions {
    private boolean showScoreboard = true;
    private boolean receiveDuelRequests = true;
    private boolean allowSpectators = true;
    private boolean showPlayers = false;
    private KillEffects killEffect = KillEffects.NONE;
    private boolean scoreboardLines = true;
    private boolean menuSounds = false;
    private Colors theme = Colors.valueOf(Praxi.getInstance().getMainConfig().getString("DEFAULT-THEME-COLOR"));
    private int pingRange = 250;
}
