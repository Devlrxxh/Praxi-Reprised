package me.funky.praxi.duel;

import me.funky.praxi.Locale;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.profile.Profile;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.util.ChatComponentBuilder;
import me.funky.praxi.util.ChatHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DuelProcedure {

	@Getter private Player sender;
	@Getter private UUID target;
	@Getter private final boolean party;
	@Getter @Setter private Kit kit;
	@Getter @Setter private Arena arena;

	public DuelProcedure(Player sender, Player target, boolean party) {
		this.sender = sender;
		this.target = target.getUniqueId();
		this.party = party;
	}

	public void send() {
		Player target = Bukkit.getPlayer(this.target);

		if (!sender.isOnline() || target == null || !target.isOnline()) {
			return;
		}

		DuelRequest duelRequest = new DuelRequest(sender.getUniqueId(), target.getUniqueId(), party);
		duelRequest.setKit(kit);
		duelRequest.setArena(arena);

		Profile senderProfile = Profile.getByUuid(sender.getUniqueId());
		senderProfile.setDuelProcedure(null);

		Profile targetProfile = Profile.getByUuid(target.getUniqueId());
		targetProfile.getDuelRequests().add(duelRequest);

		if (party) {
			sender.sendMessage(Locale.DUEL_SENT_PARTY.format(kit.getName(), target.getName(),
					targetProfile.getParty().getPlayers().size(), arena.getName()));

			for (String msg : Locale.DUEL_RECEIVED_PARTY.formatLines(kit.getName(), sender.getName(),
					senderProfile.getParty().getPlayers().size(), arena.getName())) {
				if (msg.contains("%CLICKABLE%")) {
					ChatComponentBuilder builder = new ChatComponentBuilder(Locale.DUEL_RECEIVED_CLICKABLE.format(
							sender.getName()
					));
					builder.attachToEachPart(ChatHelper.click("/duel accept " + sender.getName()));
					builder.attachToEachPart(ChatHelper.hover(Locale.DUEL_RECEIVED_HOVER.format()));

					target.spigot().sendMessage(builder.create());
				} else {
					target.sendMessage(msg);
				}
			}
		} else {
			sender.sendMessage(Locale.DUEL_SENT.format(kit.getName(), target.getName(), arena.getName()));

			for (String msg : Locale.DUEL_RECEIVED.formatLines(kit.getName(), sender.getName(), arena.getName())) {
				if (msg.contains("%CLICKABLE%")) {
					ChatComponentBuilder builder = new ChatComponentBuilder(Locale.DUEL_RECEIVED_CLICKABLE.format(
							sender.getName()
					));
					builder.attachToEachPart(ChatHelper.click("/duel accept " + sender.getName()));
					builder.attachToEachPart(ChatHelper.hover(Locale.DUEL_RECEIVED_HOVER.format()));

					target.spigot().sendMessage(builder.create());
				} else {
					target.sendMessage(msg);
				}
			}
		}
	}

}
