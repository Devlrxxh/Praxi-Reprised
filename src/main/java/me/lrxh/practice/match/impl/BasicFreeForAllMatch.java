package me.lrxh.practice.match.impl;

import me.lrxh.practice.Locale;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.MatchSnapshot;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.meta.ProfileKitData;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ChatComponentBuilder;
import me.lrxh.practice.util.InventoryUtil;
import me.lrxh.practice.util.PlayerUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicFreeForAllMatch extends Match {

    private final List<GameParticipant<MatchGamePlayer>> participants;
    private GameParticipant<MatchGamePlayer> winningParticipant;

    public BasicFreeForAllMatch(Queue queue, Kit kit, Arena arena, List<GameParticipant<MatchGamePlayer>> participants) {
        super(queue, kit, arena, false, false);

        this.participants = participants;
    }

    @Override
    public void setupPlayer(Player player) {
        // Set the player as alive
        MatchGamePlayer gamePlayer = getGamePlayer(player);
        gamePlayer.setDead(false);

        // If the player disconnected, skip any operations for them
        if (gamePlayer.isDisconnected()) {
            return;
        }

        // Reset the player's inventory
        PlayerUtil.reset(player);

        // Deny movement if the kit is sumo
        if (getKit().getGameRules().isSumo() || getKit().getGameRules().isBedwars()) {
            PlayerUtil.denyMovement(player);
        }

        // Set the player's max damage ticks
        player.setMaximumNoDamageTicks(getKit().getGameRules().getHitDelay());

        // If the player has no kits, apply the default kit, otherwise
        // give the player a list of kit books to choose from
        if (!getKit().getGameRules().isSumo()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            ProfileKitData kitData = profile.getKitData().get(getKit());

            if (kitData.getKitCount() > 0) {
                profile.getKitData().get(getKit()).giveBooks(player);
            } else {
                GameParticipant<MatchGamePlayer> participantA = this.getParticipantA();
                player.getInventory().setArmorContents(InventoryUtil.color(getKit().getKitLoadout().getArmor(), participantA.containsPlayer(player.getUniqueId()) ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));
                //player.getInventory().setArmorContents(getKit().getKitLoadout().getArmor());
                //player.getInventory().setContents(getKit().getKitLoadout().getContents());
                player.getInventory().setContents(InventoryUtil.color(getKit().getKitLoadout().getContents(), participantA.containsPlayer(player.getUniqueId()) ? Color.RED : Color.BLUE).toArray(new ItemStack[0]));

                player.sendMessage(Locale.MATCH_GIVE_KIT.format(player, "Default", kit.getName()));
                profile.getMatch().getGamePlayer(player).setKitLoadout(kit.getKitLoadout());
            }
        }

        // Teleport the player to their spawn point
        Location spawn = arena.getSpawnA();

        if (spawn.getBlock().getType() == Material.AIR) {
            player.teleport(spawn);
        } else {
            player.teleport(spawn.add(0, 2, 0));
        }
    }

    @Override
    public boolean canEndMatch() {
        return getRemainingTeams() <= 1;
    }

    @Override
    public boolean canStartRound() {
        return false;
    }

    @Override
    public boolean canEndRound() {
        return getRemainingTeams() <= 1;
    }

    @Override
    public void onRoundEnd() {
        for (GameParticipant<MatchGamePlayer> gameParticipant : participants) {
            if (!gameParticipant.isAllDead()) {
                winningParticipant = gameParticipant;
                break;
            }
        }

        if (!kit.getGameRules().isSumo()) {
            // Make all snapshots available
            for (MatchSnapshot snapshot : snapshots) {
                snapshot.setCreatedAt(System.currentTimeMillis());
                MatchSnapshot.getSnapshots().put(snapshot.getUuid(), snapshot);
            }
        }

        super.onRoundEnd();
    }

    @Override
    public boolean isOnSameTeam(Player first, Player second) {
        return first.equals(second);
    }

    @Override
    public List<GameParticipant<MatchGamePlayer>> getParticipants() {
        return new ArrayList<>(participants);
    }

    @Override
    public org.bukkit.ChatColor getRelationColor(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return org.bukkit.ChatColor.GREEN;
        } else {
            for (GameParticipant<MatchGamePlayer> participant : participants) {
                if (participant.containsPlayer(target.getUniqueId())) {
                    return org.bukkit.ChatColor.RED;
                }
            }

            return org.bukkit.ChatColor.YELLOW;
        }
    }

    @Override
    public void addSpectator(Player spectator, Player target) {
        super.addSpectator(spectator, target);
        Profile profile = Profile.getByUuid(spectator.getUniqueId());
        Match match = profile.getMatch();

        spectator.sendMessage(Locale.MATCH_START_SPECTATING.format(spectator, CC.GREEN, target.getUniqueId(), CC.GREEN, match.getOpponent(target.getUniqueId())));
    }

    @Override
    public void sendEndMessage(Player player) {
        List<String> formattedStrings = new ArrayList<>(Locale.MATCH_END_DETAILS.formatLines());
        for (String string : formattedStrings) {
            if (string.equalsIgnoreCase("%INVENTORIES%")) {
                ChatComponentBuilder winner = new ChatComponentBuilder(Locale.MATCH_END_WINNER_INVENTORY
                        .format(player));

                PlayerUtil.sendMessage(player, Collections.singletonList(winner).toArray(new ChatComponentBuilder[0]), getTeamAsComponent(winningParticipant),
                        getTeamAsComponent(winningParticipant));

            } else if (string.equalsIgnoreCase("%ENDMESSAGE%")) {
                formattedStrings.remove(string);
            } else if (string.equalsIgnoreCase("%ELO_CHANGES%")) {
                formattedStrings.remove(string);
            } else {
                player.sendMessage(CC.translate(string));
            }
        }
    }

//    @Override
//    public List<BaseComponent[]> generateEndComponents(Player player) {
//        List<BaseComponent[]> componentsList = new ArrayList<>();
//
//        for (String line : Locale.MATCH_END_DETAILS.formatLines()) {
//            if (line.equalsIgnoreCase("%INVENTORIES%")) {
//                List<GameParticipant<MatchGamePlayer>> participants = new ArrayList<>(this.participants);
//                participants.remove(winningParticipant);
//
//
//                BaseComponent[] winners = generateInventoriesComponents(
//                        Locale.MATCH_END_WINNER_INVENTORY.format(""), winningParticipant);
//
//                BaseComponent[] losers = generateInventoriesComponents(
//                        Locale.MATCH_END_LOSER_INVENTORY.format(participants.size() > 1 ? "s" : ""), participants);
//
//                componentsList.add(winners);
//                componentsList.add(losers);
//
//                continue;
//            }
//
//            if (line.equalsIgnoreCase("%ELO_CHANGES%")) {
//                continue;
//            }
//
//            componentsList.add(new ChatComponentBuilder("").parse(line).create());
//        }
//
//        return componentsList;
//    }

    public int getRemainingTeams() {
        int remaining = 0;

        for (GameParticipant<MatchGamePlayer> gameParticipant : participants) {
            if (!gameParticipant.isAllDead()) {
                remaining++;
            }
        }

        return remaining;
    }

}
