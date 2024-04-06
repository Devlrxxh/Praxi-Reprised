package me.lrxh.practice.party.menu;

import lombok.AllArgsConstructor;
import me.lrxh.practice.Practice;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.impl.BasicFreeForAllMatch;
import me.lrxh.practice.match.impl.BasicTeamMatch;
import me.lrxh.practice.match.participant.MatchGamePlayer;
import me.lrxh.practice.participant.GameParticipant;
import me.lrxh.practice.participant.TeamGameParticipant;
import me.lrxh.practice.party.Party;
import me.lrxh.practice.party.PartyEvent;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.filters.Filters;
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
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("PARTY.EVENTS.FILTER"));
    }


    public int getSize() {
        return Practice.getInstance().getMenusConfig().getInteger("PARTY.EVENTS.SELECT-KIT.SIZE");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int i = 10;

        for (Kit kit : Kit.getKits()) {
            if (kit.isEnabled()) {
                buttons.put(i++, new SelectKitButton(partyEvent, kit));
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
                    .name(Practice.getInstance().getMenusConfig().getString("PARTY.EVENTS.SELECT-KIT.KIT-NAME").replace("<kit>", kit.getName()))
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
                match = new BasicTeamMatch(null, kit, arena, false, participantA, participantB, false);
            }

            // Start match
            match.start();
        }

    }

}
