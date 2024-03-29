package me.lrxh.practice.profile;

import lombok.Getter;

@Getter
public enum Times {
    DAY(0, "Day"),
    NIGHT(18000, "Night"),
    SUNRISE(23000, "Sunrise"),
    SUNSET(12000, "Sunset");

    private final int time;
    private final String name;

    Times(int time, String name) {
        this.time = time;
        this.name = name;
    }
}
