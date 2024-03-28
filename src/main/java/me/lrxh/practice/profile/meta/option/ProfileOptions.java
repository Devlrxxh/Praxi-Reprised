package me.lrxh.practice.profile.meta.option;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.KillEffects;
import me.lrxh.practice.profile.Themes;
import me.lrxh.practice.profile.Times;

@Setter
@Getter
@Accessors(fluent = true)
public class ProfileOptions {
    private boolean showScoreboard = true;
    private boolean receiveDuelRequests = true;
    private boolean allowSpectators = true;
    private boolean showPlayers = false;
    private KillEffects killEffect = KillEffects.NONE;
    private boolean menuSounds = false;
    private Themes theme = Themes.valueOf(Practice.getInstance().getMainConfig().getString("DEFAULT-THEME-COLOR"));
    private int pingRange = 250;
    private Times time = Times.DAY;
}
