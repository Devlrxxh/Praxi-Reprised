package me.lrxh.practice.party.menu;

import lombok.AllArgsConstructor;
import me.lrxh.practice.Practice;
import me.lrxh.practice.party.PartyEvent;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.filters.Filters;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyEventSelectEventMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Practice.getInstance().getMenusConfig().getString("PARTY.EVENTS.TITLE");
    }

    @Override
    public int getSize() {
        return Practice.getInstance().getMenusConfig().getInteger("PARTY.EVENTS.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("PARTY.EVENTS.FILTER"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(12, new SelectEventButton(PartyEvent.FFA));
        buttons.put(14, new SelectEventButton(PartyEvent.SPLIT));
        return buttons;
    }

    @AllArgsConstructor
    private static class SelectEventButton extends Button {

        private PartyEvent partyEvent;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> splitLore = new ArrayList<>();
            splitLore.add(CC.translate("&7Split your party into"));
            splitLore.add(CC.translate("&72 teams and fight."));
            splitLore.add(CC.translate(""));
            splitLore.add(CC.translate("&aClick to host!"));

            List<String> ffaLore = new ArrayList<>();
            ffaLore.add(CC.translate("&7Everybody in the party"));
            ffaLore.add(CC.translate("&7fights everybody else."));
            ffaLore.add(CC.translate(""));
            ffaLore.add(CC.translate("&aClick to host!"));

            return new ItemBuilder(partyEvent == PartyEvent.FFA ? Material.DIAMOND_SWORD : Material.GOLD_AXE)
                    .name(Practice.getInstance().getMenusConfig().getString("PARTY.EVENTS.EVENT-COLOR").replace("<event>", partyEvent.getName()))
                    .lore(partyEvent == PartyEvent.FFA ? ffaLore : splitLore, player)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }

            new PartyEventSelectKitMenu(partyEvent).openMenu(player);
        }

    }

}
