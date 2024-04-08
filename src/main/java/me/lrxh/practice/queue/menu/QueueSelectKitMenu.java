package me.lrxh.practice.queue.menu;

import lombok.AllArgsConstructor;
import me.lrxh.practice.Practice;
import me.lrxh.practice.leaderboards.Leaderboard;
import me.lrxh.practice.leaderboards.PlayerElo;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.filters.Filters;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class QueueSelectKitMenu extends Menu {

    private boolean ranked;

    @Override
    public int getSize() {
        return Practice.getInstance().getMenusConfig().getInteger("QUEUES-MENUS.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("QUEUES-MENUS.FILTER"));
    }

    @Override
    public String getTitle(Player player) {
        return ranked ?
                Practice.getInstance().getMenusConfig().getString("QUEUES-MENUS.RANKED.TITLE") :
                Practice.getInstance().getMenusConfig().getString("QUEUES-MENUS.UNRANKED.TITLE");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int i = 10;

        for (Queue queue : Practice.getInstance().getCache().getQueues()) {
            if (queue.isRanked() == ranked) {
                buttons.put(i++, new SelectKitButton(queue));
            }
        }
        //buttons.put(4, new SelectQueueButton(ranked));
        return buttons;
    }

    private static class SelectQueueButton extends Button {
        private final boolean ranked;

        public SelectQueueButton(boolean ranked) {
            this.ranked = ranked;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.REDSTONE_COMPARATOR).name("&aRandom Queue").clearEnchantments().clearFlags().clearFlags().build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            ArrayList<Queue> queues = new ArrayList<>(Practice.getInstance().getCache().getQueues());
            Random rand = new Random();
            Queue randomQueue = queues.get(rand.nextInt(queues.size()));

            player.closeInventory();
            randomQueue.addPlayer(player, randomQueue.isRanked() ? profile.getKitData().get(randomQueue.getKit()).getElo() : 0, !ranked);
            randomQueue.addQueue();
        }
    }


    @AllArgsConstructor
    private class SelectKitButton extends Button {
        private Queue queue;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            List<String> configLore = ranked ?
                    Practice.getInstance().getMenusConfig().getStringList("QUEUES-MENUS.RANKED.LORE") :
                    Practice.getInstance().getMenusConfig().getStringList("QUEUES-MENUS.UNRANKED.LORE");

            configLore.forEach(line -> {
                line = line.replaceAll("<playing>", String.valueOf(Match.getInFightsCount(queue)));
                line = line.replaceAll("<queueing>", String.valueOf(queue.getQueuing()));
                line = line.replaceAll("<kit>", queue.getKit().getName());
                line = replaceLeaderboardPlaceholders(line, queue);
                if (line.contains("<description>")) {
                    if (!queue.getKit().getDescription().contains("none")) {
                        List<String> descriptionLines = queue.getKit().getDescription();
                        for (String descriptionLine : descriptionLines) {
                            lore.add(line.replaceAll("<description>", descriptionLine));
                        }
                    }
                } else {
                    lore.add(line);
                }

            });

            String kitName = Practice.getInstance().getMenusConfig().getString(ranked ?
                            "QUEUES-MENUS.RANKED.KIT-NAME" :
                            "QUEUES-MENUS.UNRANKED.KIT-NAME")
                    .replace("<kit>", queue.getKit().getName())
                    .replace("<type>", ranked ? "Unranked" : "Ranked");

            return new ItemBuilder(queue.getKit().getDisplayIcon())
                    .name(kitName)
                    .lore(lore, player)
                    .amount(Match.getInFightsCount(queue), true)
                    .clearFlags()
                    .build();
        }

        private String replaceLeaderboardPlaceholders(String line, Queue queue) {
            if (line.contains("<lb_")) {
                Matcher matcher = Pattern.compile("<lb_(\\d+)_(\\w+)_>").matcher(line);
                while (matcher.find()) {
                    int position = Integer.parseInt(matcher.group(1));
                    String valueType = matcher.group(2);
                    boolean showName = line.contains("_name_");

                    if (position > 10 || position < 1) return line;

                    PlayerElo playerElo = Leaderboard.getEloLeaderboards().get(queue.getKit().getName()).getTopEloPlayers().get(position - 1);
                    PlayerElo playerKills = Leaderboard.getEloLeaderboards().get(queue.getKit().getName()).getTopKillPlayers().get(position - 1);

                    switch (valueType) {
                        case "name":
                            line = showName ?
                                    line.replace("<lb_" + position + "_name_>", playerElo.getPlayerName()) :
                                    line.replace("<lb_" + position + "_name_>", playerKills.getPlayerName());
                            break;
                        case "elo":
                            line = line.replace("<lb_" + position + "_elo_>", String.valueOf(playerElo.getElo()));
                            break;
                        case "kills":
                            line = line.replace("<lb_" + position + "_kills_>", String.valueOf(playerKills.getKills()));
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
            if (!profile.getFollowing().isEmpty()) {
                player.sendMessage(CC.translate("&4ERROR - &cYou cannot queue while following someone!"));
                player.closeInventory();
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
            queue.addPlayer(player, queue.isRanked() ? profile.getKitData().get(queue.getKit()).getElo() : 0, ranked);
            queue.addQueue();
        }
    }
}
