package me.lrxh.practice.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EntityUtils {
    private int currentFakeEntityId = -1;

    public int getFakeEntityId() {
        return currentFakeEntityId--;
    }
}
