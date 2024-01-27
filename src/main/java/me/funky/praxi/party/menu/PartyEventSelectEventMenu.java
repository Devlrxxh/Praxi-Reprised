package me.funky.praxi.party.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.party.PartyEvent;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PartyEventSelectEventMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("PARTY.EVENTS.TITLE");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(3, new SelectEventButton(PartyEvent.FFA));
        buttons.put(5, new SelectEventButton(PartyEvent.SPLIT));
        return buttons;
    }

    @AllArgsConstructor
    private class SelectEventButton extends Button {

        private PartyEvent partyEvent;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(partyEvent == PartyEvent.FFA ? Material.QUARTZ : Material.REDSTONE)
                    .name(Praxi.getInstance().getMenusConfig().getString("PARTY.EVENTS.EVENT-COLOR").replace("<event>", partyEvent.getName()))
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
