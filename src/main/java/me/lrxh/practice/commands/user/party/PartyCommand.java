package me.lrxh.practice.commands.user.party;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.party.Party;
import me.lrxh.practice.party.PartyPrivacy;
import me.lrxh.practice.party.menu.OtherPartiesMenu;
import me.lrxh.practice.party.menu.PartyEventSelectEventMenu;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("party")
@Description("Party Command.")
public class PartyCommand extends BaseCommand {

    @Subcommand("chat")
    @Syntax("<message>")
    public void chat(Player player, String message) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() != null) {
            profile.getParty().sendChat(player, message);
        }
    }

    @Subcommand("close")
    public void close(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!profile.getParty().getLeader().equals(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        profile.getParty().setPrivacy(PartyPrivacy.CLOSED);
    }

    @Subcommand("create")
    public void create(Player player) {
        if (player.hasMetadata("frozen")) {
            player.sendMessage(CC.RED + "You cannot create a party while frozen.");
            return;
        }

        if (Practice.getInstance().isReplay()) {
            if (PlayerUtil.inReplay(player)) {
                player.sendMessage(CC.RED + "You cannot create a party while in replay.");
                return;
            }
        }
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() != null) {
            player.sendMessage(CC.RED + "You already have a party.");
            return;
        }


        if (profile.getState() != ProfileState.LOBBY) {
            player.sendMessage(CC.RED + "You must be in the lobby to create a party.");
            return;
        }

        PlayerUtil.setInParty(player, true);
        profile.setParty(new Party(player));

        Practice.getInstance().getHotbar().giveHotbarItems(player);

        player.sendMessage(Locale.PARTY_CREATE.format(player));
    }


    @Subcommand("disband")
    public void disband(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!profile.getParty().getLeader().equals(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }
        PlayerUtil.setInParty(player, false);

        profile.getParty().disband();
    }

    @Default
    @Subcommand("help")
    public void help(Player player) {
        Locale.PARTY_HELP.formatLines(player).forEach(player::sendMessage);
    }

    @Subcommand("info")
    public void info(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        profile.getParty().sendInformation(player);
    }

    @Syntax("<name>")
    @Subcommand("invite")
    @CommandCompletion("@names")

    public void invite(Player player, String playerName) {

        if (Bukkit.getPlayer(playerName) == null) {
            player.sendMessage(CC.translate("&4ERROR - &cPlayer isn't online!"));
            return;
        }
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            player.sendMessage(CC.RED + "A player with that name could not be found.");
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!profile.getParty().getLeader().equals(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        if (profile.getParty().getInvite(target.getUniqueId()) != null) {
            player.sendMessage(CC.RED + "That player has already been invited to your party.");
            return;
        }

        if (profile.getParty().containsPlayer(target.getUniqueId())) {
            player.sendMessage(CC.RED + "That player is already in your party.");
            return;
        }

        if (profile.getParty().getPrivacy() == PartyPrivacy.OPEN) {
            player.sendMessage(CC.RED + "The party state is Open. You do not need to invite players.");
            return;
        }

        Profile targetData = Profile.getByUuid(target.getUniqueId());

        if (targetData.isBusy()) {
            player.sendMessage(target.getDisplayName() + CC.RED + " is currently busy.");
            return;
        }

        profile.getParty().invite(target);
    }

    @Syntax("<name>")
    @Subcommand("join")
    @CommandCompletion("@names")
    public void join(Player player, String playerName) {

        if (Bukkit.getPlayer(playerName) == null) {
            player.sendMessage(CC.translate("&4ERROR - &cPlayer isn't online!"));
            return;
        }
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            player.sendMessage(CC.RED + "A player with that name could not be found.");
            return;
        }


        if (player.hasMetadata("frozen")) {
            player.sendMessage(CC.RED + "You cannot join a party while frozen.");
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() != null) {
            player.sendMessage(CC.RED + "You already have a party.");
            return;
        }


        Profile targetProfile = Profile.getByUuid(target.getUniqueId());
        Party party = targetProfile.getParty();

        if (party == null) {
            player.sendMessage(CC.RED + "A party with that name could not be found.");
            return;
        }

        if (party.getPrivacy() == PartyPrivacy.CLOSED) {
            if (party.getInvite(player.getUniqueId()) == null) {
                player.sendMessage(CC.RED + "You have not been invited to that party.");
                return;
            }
        }

        if (party.getPlayers().size() >= 32) {
            player.sendMessage(CC.RED + "That party is full and cannot hold anymore players.");
            return;
        }
        PlayerUtil.setInParty(player, true);

        party.join(player);
    }

    @Syntax("<name>")
    @Subcommand("kick")
    @CommandCompletion("@names")
    public void kick(Player player, String playerName) {

        if (Bukkit.getPlayer(playerName) == null) {
            player.sendMessage(CC.translate("&4ERROR - &cPlayer isn't online!"));
            return;
        }
        Player target = Bukkit.getPlayer(playerName);
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!profile.getParty().getLeader().equals(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        if (!profile.getParty().containsPlayer(target.getUniqueId())) {
            player.sendMessage(CC.RED + "That player is not a member of your party.");
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(CC.RED + "You cannot kick yourself from your party.");
            return;
        }
        PlayerUtil.setInParty(target, false);

        profile.getParty().leave(target, true);
    }

    @Subcommand("event")
    public void event(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!profile.getParty().getLeader().equals(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        new PartyEventSelectEventMenu().openMenu(player);
    }

    @Subcommand("other")
    public void other(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!profile.getParty().getLeader().equals(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        new OtherPartiesMenu().openMenu(player);
    }

    @Subcommand("leave")
    public void leave(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (profile.getParty().getLeader().equals(player)) {
            PlayerUtil.setInParty(player, false);
            profile.getParty().disband();
        } else {
            PlayerUtil.setInParty(player, false);
            profile.getParty().leave(player, false);
        }
    }

    @Subcommand("open")
    public void open(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!profile.getParty().getLeader().equals(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        profile.getParty().setPrivacy(PartyPrivacy.OPEN);
    }
}
