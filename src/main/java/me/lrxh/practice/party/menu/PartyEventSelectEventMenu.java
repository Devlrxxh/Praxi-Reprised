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

import java.util.HashMap;
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
            return new ItemBuilder(partyEvent == PartyEvent.FFA ? Material.QUARTZ : Material.REDSTONE)
                    .name(Practice.getInstance().getMenusConfig().getString("PARTY.EVENTS.EVENT-COLOR").replace("<event>", partyEvent.getName()))
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
