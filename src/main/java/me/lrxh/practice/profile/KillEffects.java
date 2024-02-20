package me.lrxh.practice.profile;

import lombok.Getter;

@Getter
public enum KillEffects {
    NONE("None"),
    LIGHTNING("Lightning"),
    FIREWORKS("Fireworks");

    private final String displayName;

    KillEffects(String displayName) {
        this.displayName = displayName;
    }
}
