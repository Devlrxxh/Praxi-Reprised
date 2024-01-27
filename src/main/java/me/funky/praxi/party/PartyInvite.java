package me.funky.praxi.party;

import java.util.UUID;
import lombok.Getter;

public class PartyInvite {

	@Getter private final UUID uuid;
	private final long expiresAt = System.currentTimeMillis() + 30_000L;

	public PartyInvite(UUID uuid) {
		this.uuid = uuid;
	}

	public boolean hasExpired() {
		return System.currentTimeMillis() >= expiresAt;
	}

}
