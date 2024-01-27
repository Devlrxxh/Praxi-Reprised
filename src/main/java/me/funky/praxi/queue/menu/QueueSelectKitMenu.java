package me.funky.praxi.queue.menu;

import me.funky.praxi.match.Match;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.queue.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class QueueSelectKitMenu extends Menu {

	private boolean ranked;

	{
		setAutoUpdate(true);
	}

	@Override
	public String getTitle(Player player) {
		return "&6&lSelect a kit (" + (ranked ? "Ranked" : "Unranked") + ")";
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		Map<Integer, Button> buttons = new HashMap<>();

		int i = 0;

		for (Queue queue : Queue.getQueues()) {
			if (queue.isRanked() == ranked) {
				buttons.put(i++, new SelectKitButton(queue));
			}
		}

		return buttons;
	}

	@AllArgsConstructor
	private class SelectKitButton extends Button {

		private Queue queue;

		@Override
		public ItemStack getButtonItem(Player player) {
			List<String> lore = new ArrayList<>();
			lore.add("&cFighting: &r" + Match.getInFightsCount(queue));
			lore.add("&cQueueing: &r" + queue.getPlayers().size());

			return new ItemBuilder(queue.getKit().getDisplayIcon())
					.name("&4&l" + queue.getKit().getName())
					.lore(lore)
					.build();
		}

		@Override
		public void clicked(Player player, ClickType clickType) {
			Profile profile = Profile.getByUuid(player.getUniqueId());

			if (profile == null) {
				return;
			}

			if (player.hasMetadata("frozen")) {
				player.sendMessage(CC.RED + "You cannot queue while frozen.");
				return;
			}

			if (profile.isBusy()) {
				player.sendMessage(CC.RED + "You cannot queue right now.");
				return;
			}

			player.closeInventory();

			queue.addPlayer(player, queue.isRanked() ? profile.getKitData().get(queue.getKit()).getElo() : 0);
		}

	}
}
