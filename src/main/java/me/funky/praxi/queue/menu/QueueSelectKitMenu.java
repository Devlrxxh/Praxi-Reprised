package me.funky.praxi.queue.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.leaderboards.Leaderboard;
import me.funky.praxi.leaderboards.PlayerElo;
import me.funky.praxi.match.Match;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.queue.Queue;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class QueueSelectKitMenu extends Menu {

    private boolean ranked;

    {
        setAutoUpdate(true);
    }

    @Override
    public int getSize() {
        return Praxi.getInstance().getMenusConfig().getInteger("QUEUES-MENUS.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Praxi.getInstance().getMenusConfig().getString("QUEUES-MENUS.FILTER"));
    }

    @Override
    public String getTitle(Player player) {
        return ranked ?
                Praxi.getInstance().getMenusConfig().getString("QUEUES-MENUS.RANKED.TITLE") :
                Praxi.getInstance().getMenusConfig().getString("QUEUES-MENUS.UNRANKED.TITLE");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int i = 10;

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
            List<String> configLore = ranked ?
                    Praxi.getInstance().getMenusConfig().getStringList("QUEUES-MENUS.RANKED.LORE") :
                    Praxi.getInstance().getMenusConfig().getStringList("QUEUES-MENUS.UNRANKED.LORE");

            configLore.forEach(line -> {
                line = line.replaceAll("<playing>", String.valueOf(Match.getInFightsCount(queue)));
                line = line.replaceAll("<queueing>", String.valueOf(queue.getKit().getQueuing()));
                line = replaceLeaderboardPlaceholders(line, queue);
                if (!line.contains("<description>") || !queue.getKit().getDescription().equalsIgnoreCase("none")) {
                    line = line.replaceAll("<description>", queue.getKit().getDescription());
                    lore.add(line);
                }
            });

            String kitName = Praxi.getInstance().getMenusConfig().getString(ranked ?
                            "QUEUES-MENUS.RANKED.KIT-NAME" :
                            "QUEUES-MENUS.UNRANKED.KIT-NAME")
                    .replace("<kit>", queue.getKit().getName())
                    .replace("<type>", ranked ? "Unranked" : "Ranked");

            return new ItemBuilder(queue.getKit().getDisplayIcon())
                    .name(kitName)
                    .lore(lore)
                    .clearEnchantments()
                    .clearFlags()
                    .build();
        }

        private String replaceLeaderboardPlaceholders(String line, Queue queue) {
            if (line.contains("<lb_")) {
                Matcher matcher = Pattern.compile("<lb_(\\d+)_(\\w+)_>").matcher(line);
                while (matcher.find()) {
                    int position = Integer.parseInt(matcher.group(1));
                    String placeholder = matcher.group(2);
                    PlayerElo playerElo = Leaderboard.getEloLeaderboards().get(queue.getKit().getName()).getTopPlayers().get(position - 1);
                    switch (placeholder) {
                        case "name":
                            line = line.replace("<lb_" + position + "_name_>", playerElo.getPlayerName());
                            break;
                        case "elo":
                            line = line.replace("<lb_" + position + "_elo_>", String.valueOf(playerElo.getElo()));
                            break;
                        case "kills":
                            line = line.replace("<lb_" + position + "_kills_>", String.valueOf(playerElo.getKills()));
                            break;
                        default:
                            break;
                    }
                }
            }
            return line;
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
            queue.getKit().addQueue();
        }
    }
}
