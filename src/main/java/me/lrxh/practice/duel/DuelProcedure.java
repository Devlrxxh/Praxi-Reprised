package me.lrxh.practice.duel;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.BukkitReflection;
import me.lrxh.practice.util.ChatComponentBuilder;
import me.lrxh.practice.util.ChatHelper;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class DuelProcedure {

    private final boolean party;
    private final Player sender;
    private final UUID target;
    @Setter
    private Kit kit;
    @Setter
    private Arena arena;

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
            sender.sendMessage(Locale.DUEL_SENT_PARTY.format(target, kit.getName(), target.getName(),
                    targetProfile.getParty().getPlayers().size(), arena.getDisplayName()));

            for (String msg : Locale.DUEL_RECEIVED_PARTY.formatLines(sender, kit.getName(), sender.getName(),
                    senderProfile.getParty().getPlayers().size(), arena.getDisplayName())) {
                if (msg.contains("%CLICKABLE%")) {
                    ChatComponentBuilder builder = new ChatComponentBuilder(Locale.DUEL_RECEIVED_CLICKABLE.format(target,
                            sender.getName()
                    ));
                    builder.attachToEachPart(ChatHelper.click("/duel accept " + sender.getName()));
                    builder.attachToEachPart(ChatHelper.hover(Locale.DUEL_RECEIVED_HOVER.format(target)));

                    target.spigot().sendMessage(builder.create());
                } else {
                    target.sendMessage(msg);
                }
            }
        } else {
            sender.sendMessage(Locale.DUEL_SENT.format(sender, kit.getName(), target.getName(), arena.getDisplayName()));

            for (String msg : Locale.DUEL_RECEIVED.formatLines(sender, kit.getName(), sender.getName(), arena.getDisplayName(), BukkitReflection.getPing(sender))) {
                if (msg.contains("%CLICKABLE%")) {
                    ChatComponentBuilder builder = new ChatComponentBuilder(Locale.DUEL_RECEIVED_CLICKABLE.format(target,
                            sender.getName()
                    ));
                    builder.attachToEachPart(ChatHelper.click("/duel accept " + sender.getName()));
                    builder.attachToEachPart(ChatHelper.hover(Locale.DUEL_RECEIVED_HOVER.format(target)));

                    target.spigot().sendMessage(builder.create());
                    target.playSound(target.getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 1.0f);
                } else {
                    target.sendMessage(msg);
                }
            }
        }
    }

}
