package me.funky.praxi.queue.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.match.Match;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.queue.Queue;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class QueueSelectKitMenu extends Menu {

    private boolean ranked;

    {
        setAutoUpdate(true);
    }

    @Override
    public String getTitle(Player player) {
        if (!ranked) {
            return Praxi.getInstance().getMenusConfig().getString("QUEUES-MENUS.UNRANKED.TITLE");
        } else {
            return Praxi.getInstance().getMenusConfig().getString("QUEUES-MENUS.RANKED.TITLE");
        }
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int i = 0;

        for (Queue queue : Praxi.getInstance().getCache().getQueues()) {
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
            if (!ranked) {
                for (String line : Praxi.getInstance().getMenusConfig().getStringList("QUEUES-MENUS.UNRANKED.LORE")) {
                    line = line.replaceAll("<playing>", String.valueOf(Match.getInFightsCount(queue)));
                    line = line.replaceAll("<queueing>", String.valueOf(queue.getKit().getQueuing()));
                    lore.add(line);
                }
            } else {
                for (String line : Praxi.getInstance().getMenusConfig().getStringList("QUEUES-MENUS.RANKED.LORE")) {
                    line = line.replaceAll("<playing>", String.valueOf(Match.getInFightsCount(queue)));
                    line = line.replaceAll("<queueing>", String.valueOf(queue.getKit().getQueuing()));
                    lore.add(line);
                }
            }


            if (!ranked) {
                return new ItemBuilder(queue.getKit().getDisplayIcon())
                        .name(Praxi.getInstance().getMenusConfig().getString("QUEUES-MENUS.UNRANKED.KIT-NAME").replace("<kit>", queue.getKit().getName()))
                        .lore(lore)
                        .clearEnchantments()
                        .clearFlags()
                        .clearFlags()
                    .build();            } else {
                return new ItemBuilder(queue.getKit().getDisplayIcon())
                        .name(Praxi.getInstance().getMenusConfig().getString("QUEUES-MENUS.RANKED.KIT-NAME").replace("<kit>", queue.getKit().getName()))
                        .lore(lore)
                        .clearEnchantments()
                        .clearFlags()
                        .clearFlags()
                    .build();            }

        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (player.hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot queue while frozen.");
                return;
            }

            if (profile.isBusy()) {
                player.sendMessage(CC.RED + "You cannot queue right now.");
                return;
            }

            player.closeInventory();

            queue.addPlayer(player, queue.isRanked() ? profile.getKitData().get(queue.getKit()).getElo() : 0, ranked);
            queue.getKit().addQueue((byte) 1);
        }

    }
}
