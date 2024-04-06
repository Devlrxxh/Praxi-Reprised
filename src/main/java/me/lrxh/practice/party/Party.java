package me.lrxh.practice.party;

import lombok.Getter;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.duel.DuelRequest;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.profile.visibility.VisibilityLogic;
import me.lrxh.practice.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Getter
public class Party {

    @Getter
    private static final List<Party> parties = new ArrayList<>();

    private final Player leader;
    private final List<UUID> players;
    private final List<PartyInvite> invites;
    private PartyPrivacy privacy;

    public Party(Player player) {
        this.leader = player;
        this.players = new ArrayList<>();
        this.invites = new ArrayList<>();
        this.privacy = PartyPrivacy.CLOSED;

        this.players.add(player.getUniqueId());

        parties.add(this);
    }

    public static void init() {
        // Remove expired invites from each party every 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                Party.getParties().forEach(party -> party.getInvites().removeIf(PartyInvite::hasExpired));
            }
        }.runTaskTimerAsynchronously(Practice.getInstance(), 20L * 2, 20L * 2);
    }

    public void setPrivacy(PartyPrivacy privacy) {
        this.privacy = privacy;

        sendMessage(Locale.PARTY_PRIVACY_CHANGE.format(privacy.getReadable()));
    }

    public boolean containsPlayer(UUID uuid) {
        return players.contains(uuid);
    }

    public PartyInvite getInvite(UUID uuid) {
        Iterator<PartyInvite> iterator = invites.iterator();

        while (iterator.hasNext()) {
            PartyInvite invite = iterator.next();

            if (invite.getUuid().equals(uuid)) {
                if (invite.hasExpired()) {
                    iterator.remove();
                    return null;
                } else {
                    return invite;
                }
            }
        }

        return null;
    }

    public void invite(Player target) {
        invites.add(new PartyInvite(target.getUniqueId()));

        for (String msg : Locale.PARTY_INVITE.formatLines(leader.getName(), (leader.getName()))) {
            if (msg.contains("%CLICKABLE%")) {
                msg = msg.replace("%CLICKABLE%", "");

                target.spigot().sendMessage(new ChatComponentBuilder("")
                        .parse(msg)
                        .attachToEachPart(ChatHelper.click("/party join " + leader.getName()))
                        .attachToEachPart(ChatHelper.hover(Locale.PARTY_INVITE_HOVER.format(target)))
                        .create());
            } else {
                target.sendMessage(msg);
            }
        }

        sendMessage(Locale.PARTY_INVITE_BROADCAST.format((target.getName())));
    }

    public void join(Player player) {
        invites.removeIf(invite -> invite.getUuid().equals(player.getUniqueId()));
        players.add(player.getUniqueId());

        sendMessage(Locale.PARTY_JOIN.format(player, player.getName()));

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setParty(this);

        if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.QUEUEING) {
            Practice.getInstance().getHotbar().giveHotbarItems(player);

            for (Player otherPlayer : getListOfPlayers()) {
                VisibilityLogic.handle(player, otherPlayer);
            }
        }

        VisibilityLogic.handle(player);

        for (Player otherPlayer : getListOfPlayers()) {
            VisibilityLogic.handle(otherPlayer, player);
        }
    }

    public void leave(Player player, boolean kick) {
        sendMessage(Locale.PARTY_LEAVE.format(player, (player.getName()), (kick ? "been kicked from" : "left")));

        players.removeIf(uuid -> uuid.equals(player.getUniqueId()));

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setParty(null);

        if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.QUEUEING) {
            Practice.getInstance().getHotbar().giveHotbarItems(player);
        }

        VisibilityLogic.handle(player);

        for (Player otherPlayer : getListOfPlayers()) {
            VisibilityLogic.handle(otherPlayer, player);
        }
    }

    public void disband() {
        parties.remove(this);

        sendMessage(Locale.PARTY_DISBAND.format());

        // Remove any party duel requests
        Profile leaderProfile = Profile.getByUuid(leader.getUniqueId());

        leaderProfile.getDuelRequests().removeIf(DuelRequest::isParty);

        // Reset player profiles
        for (Player player : getListOfPlayers()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setParty(null);
            PlayerUtil.setInParty(player, false);

            if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.QUEUEING) {
                Practice.getInstance().getHotbar().giveHotbarItems(player);
            }
        }

        for (Player player : getListOfPlayers()) {
            VisibilityLogic.handle(player);
        }
    }

    public void sendInformation(Player sendTo) {
        StringBuilder builder = new StringBuilder();

        for (Player player : getListOfPlayers()) {
            builder.append(player.getName())
                    .append(CC.GRAY)
                    .append(", ");
        }

        for (String line : Locale.PARTY_INFORMATION.formatLines(sendTo, privacy.getReadable(), leader.getName(), getPlayers().size(), builder.substring(0, builder.length() - 2))) {
            sendTo.sendMessage(CC.translate(line));
        }
    }

    public void sendChat(Player player, String message) {
        sendMessage(Locale.PARTY_CHAT_PREFIX.format(player) +
                player.getDisplayName() + ChatColor.RESET + ": " + message);
    }

    public void sendMessage(String message) {
        for (Player player : getListOfPlayers()) {
            ArrayList<String> list = new ArrayList<>();
            list.add(CC.translate(message));
            player.sendMessage(PlaceholderUtil.format(list, player).toString().replace("[", "").replace("]", ""));
        }
    }

    public List<Player> getListOfPlayers() {
        List<Player> players = new ArrayList<>();

        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                players.add(player);
            }
        }

        return players;
    }

}
