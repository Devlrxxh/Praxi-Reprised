package me.funky.praxi.kit.meta;

import lombok.Getter;
import lombok.Setter;

public class KitGameRules {

	@Getter @Setter private boolean build;
	@Getter @Setter private boolean spleef;
	@Getter @Setter private boolean sumo;
	@Getter @Setter private boolean parkour;
	@Getter @Setter private boolean healthRegeneration;
	@Getter @Setter private boolean showHealth;
	@Getter @Setter private int hitDelay = 20;
	@Getter @Setter private String kbProfile;

}
