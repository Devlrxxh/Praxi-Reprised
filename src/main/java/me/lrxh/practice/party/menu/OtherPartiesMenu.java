package me.lrxh.practice.party.menu;

import lombok.AllArgsConstructor;
import me.lrxh.practice.Practice;
import me.lrxh.practice.party.Party;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.filters.Filters;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherPartiesMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Practice.getInstance().getMenusConfig().getString("PARTY.OTHER-PARTIES.TITLE");
    }

    @Override
    public int getSize() {
        return Practice.getInstance().getMenusConfig().getInteger("PARTY.OTHER-PARTIES.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("PARTY.FILTER"));
    }


    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        Map<Integer, Button> buttons = new HashMap<>();

        Party.getParties().forEach(party -> {
            if (!party.equals(profile.getParty())) {
                buttons.put(buttons.size(), new PartyDisplayButton(party));
            }
        });

        return buttons;
    }

    @AllArgsConstructor
    public static class PartyDisplayButton extends Button {

        private Party party;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            int added = 0;

            for (Player partyPlayer : party.getListOfPlayers()) {
                if (added >= 10) {
                    break;
                }

                lore.add(CC.GRAY + " - " + CC.RESET + partyPlayer.getPlayer().getName());

                added++;
            }

            if (party.getPlayers().size() != added) {
                lore.add(CC.GRAY + " and " + (party.getPlayers().size() - added) + " others...");
            }

            return new ItemBuilder(Material.SKULL_ITEM)
                    .name("&6Party of &r" + party.getLeader().getName())
                    .amount(party.getPlayers().size(), false)
                    .durability(3)
                    .lore(lore, player)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() != null) {
                if (!profile.getParty().equals(party)) {
                    if (profile.getParty().getLeader().equals(player)) {
                        player.chat("/duel " + party.getLeader().getName());
                    } else {
                        player.sendMessage(ChatColor.RED + "You are not the leader of your party.");
                    }
                }
            }
        }

    }
}
