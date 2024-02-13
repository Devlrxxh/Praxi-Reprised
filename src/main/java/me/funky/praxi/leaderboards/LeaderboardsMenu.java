package me.funky.praxi.leaderboards;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.queue.Queue;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.filters.Filters;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class LeaderboardsMenu extends Menu {

    {
        setAutoUpdate(true);
    }

    @Override
    public int getSize() {
        return Praxi.getInstance().getMenusConfig().getInteger("LEADERBOARD.SIZE");
    }

    @Override
    public Filters getFilter() {
        return Filters.valueOf(Praxi.getInstance().getMenusConfig().getString("LEADERBOARD.FILTER"));
    }

    @Override
    public String getTitle(Player player) {
        return Praxi.getInstance().getMenusConfig().getString("LEADERBOARD.TITLE");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int i = 10;

        for (Queue queue : Praxi.getInstance().getCache().getQueues()) {
            if (!queue.isRanked()) {
                buttons.put(i++, new LeaderboardKitButton(queue));
            }
        }
        buttons.put(4, new GlobalStatsButton());

        return buttons;
    }

    @AllArgsConstructor
    private class GlobalStatsButton extends Button {
        public ItemStack getButtonItem(Player player) {

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwner(player.getName());
            head.setItemMeta(skullMeta);

            List<String> lore = new ArrayList<>();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            for (String line : Praxi.getInstance().getMenusConfig().getStringList("LEADERBOARD.GLOBAL-STATS.LORE")) {
                line = line.replaceAll("<wins>", String.valueOf(profile.getWins()));
                line = line.replaceAll("<loses>", String.valueOf(profile.getLoses()));
                line = line.replaceAll("<elo>", String.valueOf(profile.getElo()));
                lore.add(line);

            }


            return new ItemBuilder(head)
                    .name(Praxi.getInstance().getMenusConfig().getString("LEADERBOARD.GLOBAL-STATS.GLOBAL-STATS-NAME"))
                    .lore(lore)
                    .clearEnchantments()
                    .clearFlags()
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private class LeaderboardKitButton extends Button {

        private Queue queue;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add("");

            for (int i = 1; i <= 10; i++) {
                PlayerElo playerElo = Leaderboard.getEloLeaderboards().get(queue.getKit().getName()).getTopPlayers().get(i - 1);

                lore.add(CC.translate("&b" + i + ". &f" + playerElo.getPlayerName() + "&7- &b" + playerElo.getElo()));
            }

            return new ItemBuilder(queue.getKit().getDisplayIcon())
                    .name(Praxi.getInstance().getMenusConfig().getString("LEADERBOARD.KIT-NAME")
                            .replace("<kit>", queue.getKit().getName()))
                    .lore(lore)
                    .clearEnchantments()
                    .clearFlags()
                    .clearFlags()
                    .build();

        }

    }
}
