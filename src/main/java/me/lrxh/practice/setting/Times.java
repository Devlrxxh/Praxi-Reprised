package me.lrxh.practice.setting;

import lombok.Getter;

@Getter
public enum Times {
    DAY(0),
    NIGHT(18000),
    SUNRISE(23000),
    SUNSET(12000);

    private final int time;

    Times(int time) {
        this.time = time;
    }
}
