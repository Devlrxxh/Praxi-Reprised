package me.lrxh.practice.kit.meta;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KitGameRules {
    private boolean build;
    private boolean spleef;
    private boolean sumo;
    private boolean parkour;
    private boolean healthRegeneration;
    private boolean showHealth;
    private boolean boxing;
    private boolean bedwars;
    private int hitDelay = 20;
    private String kbProfile;
}
