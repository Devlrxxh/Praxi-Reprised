package me.lrxh.practice.leaderboards;

import lombok.AllArgsConstructor;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.queue.Queue;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.ItemBuilder;
import me.lrxh.practice.util.PlayerUtil;
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

@AllArgsConstructor
public class LeaderboardsMenu extends Menu {

    private boolean kills;

    @Override
    public int getSize() {
        return Practice.getInstance().getMenusConfig().getInteger("LEADERBOARD.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.FILTER"));
    }

    public boolean getFixedPositions() {
        return false;
    }

    public boolean resetCursor() {
        return false;
    }

    @Override
    public String getTitle(Player player) {
        return Practice.getInstance().getMenusConfig().getString("LEADERBOARD.TITLE");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int i = 10;
        for (Queue queue : Practice.getInstance().getCache().getQueues()) {
            if (!queue.isRanked()) {
                if (!kills) {
                    buttons.put(i++, new LeaderboardEloButton(queue));
                } else {
                    buttons.put(i++, new LeaderboardKillsButton(queue));
                }
            }
        }

        buttons.put(4, new GlobalStatsButton());
        buttons.put(7, new SwitchLeaderBoardButton());

        return buttons;
    }

    @AllArgsConstructor
    private class GlobalStatsButton extends Button {
        public ItemStack getButtonItem(Player player) {

            List<String> lore = new ArrayList<>();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            for (String line : Practice.getInstance().getMenusConfig().getStringList("LEADERBOARD.GLOBAL-STATS.LORE")) {
                line = line.replaceAll("<wins>", String.valueOf(profile.getWins()));
                line = line.replaceAll("<loses>", String.valueOf(profile.getLoses()));
                line = line.replaceAll("<elo>", String.valueOf(profile.getElo()));
                lore.add(line);

            }


            return new ItemBuilder(PlayerUtil.getPlayerHead(player.getUniqueId()))
                    .name(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.GLOBAL-STATS.GLOBAL-STATS-NAME"))
                    .lore(lore, player)
                    .clearEnchantments()
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private class SwitchLeaderBoardButton extends Button {
        public ItemStack getButtonItem(Player player) {

            List<String> lore = new ArrayList<>();

            lore.add(CC.translate("&7Select one of the following"));
            lore.add(CC.translate("&7type of leaderboards!"));
            lore.add(CC.translate(""));
            lore.add(CC.translate("&fCurrent view:"));
            lore.add(!kills ? " &7&l▶ &aRanked ELO" : " &7&l▶ &7Ranked ELO");
            lore.add(kills ? " &7&l▶ &aUnranked Wins" : " &7&l▶ &7Unranked Wins");
            lore.add(CC.translate(""));
            lore.add(CC.translate("&aClick to switch!"));

            return new ItemBuilder(Material.COMPASS)
                    .name(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.TOGGLE_VIEW"))
                    .lore(lore, player)
                    .clearEnchantments()
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (kills) {
                player.chat("/leaderboard elo");
            } else {
                player.chat("/leaderboard kills");
            }
        }
    }

    @AllArgsConstructor
    private class LeaderboardEloButton extends Button {

        private Queue queue;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.PLAYER-ELO")
                    .replace("<player_elo>", String.valueOf(Profile.getByUuid(player.getUniqueId()).getKitData().get(queue.getKit()).getElo())));

            for (int i = 1; i <= 10; i++) {
                PlayerElo playerElo = Leaderboard.getEloLeaderboards().get(queue.getKit().getName()).getTopEloPlayers().get(i - 1);

                lore.add(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.POSITION")
                        .replace("<position>", String.valueOf(i))
                        .replace("<value>", String.valueOf(playerElo.getElo()))
                        .replace("<player>", playerElo.getPlayerName()));
            }
            return new ItemBuilder(queue.getKit().getDisplayIcon())
                    .name(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.KIT-NAME")
                            .replace("<kit>", queue.getKit().getName()))
                    .lore(lore, player)
                    .clearEnchantments()
                    .clearFlags()
                    .build();

        }

    }

    @AllArgsConstructor
    private class LeaderboardKillsButton extends Button {

        private Queue queue;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.PLAYER-KILL")
                    .replace("<player_kill>", String.valueOf(Profile.getByUuid(player.getUniqueId()).getKitData().get(queue.getKit()).getWon())));

            for (int i = 1; i <= 10; i++) {
                PlayerElo playerElo = Leaderboard.getEloLeaderboards().get(queue.getKit().getName()).getTopKillPlayers().get(i - 1);

                lore.add(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.POSITION")
                        .replace("<position>", String.valueOf(i))
                        .replace("<value>", String.valueOf(playerElo.getKills()))
                        .replace("<player>", playerElo.getPlayerName()));
            }
            return new ItemBuilder(queue.getKit().getDisplayIcon())
                    .name(Practice.getInstance().getMenusConfig().getString("LEADERBOARD.KIT-NAME")
                            .replace("<kit>", queue.getKit().getName()))
                    .lore(lore, player)
                    .clearEnchantments()
                    .clearFlags()
                    .build();

        }

    }

}
