package me.funky.praxi.party.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Practice;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.impl.BasicFreeForAllMatch;
import me.funky.praxi.match.impl.BasicTeamMatch;
import me.funky.praxi.match.participant.MatchGamePlayer;
import me.funky.praxi.participant.GameParticipant;
import me.funky.praxi.participant.TeamGameParticipant;
import me.funky.praxi.party.Party;
import me.funky.praxi.party.PartyEvent;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@AllArgsConstructor
public class PartyEventSelectKitMenu extends Menu {

    private PartyEvent partyEvent;

    @Override
    public String getTitle(Player player) {
        return Practice.getInstance().getMenusConfig().getString("PARTY.EVENTS.SELECT-KIT.TITLE");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (Kit kit : Kit.getKits()) {
            if (kit.isEnabled()) {
                buttons.put(buttons.size(), new SelectKitButton(partyEvent, kit));
            }
        }

        return buttons;
    }

    @AllArgsConstructor
    private static class SelectKitButton extends Button {

        private PartyEvent partyEvent;
        private Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(kit.getDisplayIcon())
                    .name("&6" + kit.getName())
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            player.closeInventory();

            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }

            if (profile.getParty().getPlayers().size() <= 1) {
                player.sendMessage(CC.RED + "You do not have enough players in your party to start an event.");
                return;
            }

            Party party = profile.getParty();
            Arena arena = Arena.getRandomArena(kit);

            if (arena == null) {
                player.sendMessage(CC.RED + "There are no available arenas.");
                return;
            }

            arena.setActive(true);

            Match match;

            if (partyEvent == PartyEvent.FFA) {
                List<GameParticipant<MatchGamePlayer>> participants = new ArrayList<>();

                for (Player partyPlayer : party.getListOfPlayers()) {
                    participants.add(new GameParticipant<>(
                            new MatchGamePlayer(partyPlayer.getUniqueId(), partyPlayer.getName())));
                }

                match = new BasicFreeForAllMatch(null, kit, arena, participants);
            } else {
                Player partyLeader = party.getLeader();
                Player randomLeader = Bukkit.getPlayer(party.getPlayers().get(1));

                MatchGamePlayer leaderA = new MatchGamePlayer(partyLeader.getUniqueId(), partyLeader.getName());
                MatchGamePlayer leaderB = new MatchGamePlayer(randomLeader.getUniqueId(), randomLeader.getName());

                GameParticipant<MatchGamePlayer> participantA = new TeamGameParticipant<>(leaderA);
                GameParticipant<MatchGamePlayer> participantB = new TeamGameParticipant<>(leaderB);

                List<Player> players = new ArrayList<>(party.getListOfPlayers());
                Collections.shuffle(players);

                for (Player otherPlayer : players) {
                    if (participantA.containsPlayer(otherPlayer.getUniqueId()) ||
                            participantB.containsPlayer(otherPlayer.getUniqueId())) {
                        continue;
                    }

                    MatchGamePlayer gamePlayer = new MatchGamePlayer(otherPlayer.getUniqueId(), otherPlayer.getName());

                    if (participantA.getPlayers().size() > participantB.getPlayers().size()) {
                        participantB.getPlayers().add(gamePlayer);
                    } else {
                        participantA.getPlayers().add(gamePlayer);
                    }
                }

                // Create match
                match = new BasicTeamMatch(null, kit, arena, false, participantA, participantB);
            }

            // Start match
            match.start();
        }

    }

}
