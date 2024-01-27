package me.funky.praxi.profile.meta.option.button;

import me.funky.praxi.profile.Profile;

import me.funky.praxi.profile.option.ProfileOptionButton;
import me.funky.praxi.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class DuelRequestsOptionButton extends ProfileOptionButton {

	@Override
	public ItemStack getEnabledItem(Player player) {
		return new ItemBuilder(Material.BLAZE_ROD).build();
	}

	@Override
	public ItemStack getDisabledItem(Player player) {
		return new ItemBuilder(Material.BLAZE_ROD).build();
	}

	@Override
	public String getOptionName() {
		return "&b&lDuel Requests";
	}

	@Override
	public String getDescription() {
		return "If enabled, you will receive duel requests.";
	}

	@Override
	public String getEnabledOption() {
		return "Receive duel requests";
	}

	@Override
	public String getDisabledOption() {
		return "Do not receive duel requests";
	}

	@Override
	public boolean isEnabled(Player player) {
		return Profile.getByUuid(player.getUniqueId()).getOptions().receiveDuelRequests();
	}

	@Override
	public void clicked(Player player, ClickType clickType) {
		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.getOptions().receiveDuelRequests(!profile.getOptions().receiveDuelRequests());
	}

}
