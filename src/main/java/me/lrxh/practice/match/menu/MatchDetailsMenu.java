package me.lrxh.practice.match.menu;

import lombok.AllArgsConstructor;
import me.lrxh.practice.match.MatchSnapshot;
import me.lrxh.practice.util.*;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.button.DisplayButton;
import me.lrxh.practice.util.menu.filters.Filters;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

@AllArgsConstructor
public class MatchDetailsMenu extends Menu {

    private MatchSnapshot snapshot;

    @Override
    public String getTitle(Player player) {
        return "&7Match Inventory";
    }

    @Override
    public Filters getFilter() {
        return Filters.FILL;
    }

    public boolean getFixedPositions() {
        return false;
    }

    public boolean resetCursor() {
        return false;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        ItemStack[] fixedContents = InventoryUtil.fixInventoryOrder(snapshot.getContents());

        for (int i = 0; i < fixedContents.length; i++) {
            ItemStack itemStack = fixedContents[i];

            if (itemStack != null && itemStack.getType() != Material.AIR) {
                buttons.put(i, new DisplayButton(itemStack, true));
            }
        }


        int pos = 47;

        buttons.put(pos++, new HealthButton(snapshot.getHealth()));
        buttons.put(pos++, new HungerButton(snapshot.getHunger()));
        buttons.put(pos++, new EffectsButton(snapshot.getEffects()));

        if (snapshot.shouldDisplayRemainingPotions()) {
            buttons.put(pos++, new PotionsButton(snapshot.getUsername(), snapshot.getRemainingPotions()));
        }

        buttons.put(pos, new StatisticsButton(snapshot));

        if (this.snapshot.getOpponent() != null) {
            buttons.put(53, new SwitchInventoryButton(this.snapshot.getOpponent()));
        }
        buttons.put(45, new SwitchInventoryButton(this.snapshot.getUuid()));

        return buttons;
    }

    @AllArgsConstructor
    private class HealthButton extends Button {

        private double health;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(PlayerUtil.getPlayerHead(snapshot.getUuid()))
                    .name("&bHealth: &f" + health + "/10 &4" + StringEscapeUtils.unescapeJava("❤"))
                    .amount((int) (health == 0 ? 1 : health), false)
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private class EffectsButton extends Button {

        private Collection<PotionEffect> effects;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.BREWING_STAND_ITEM).name("&bPotion Effects");

            if (effects.isEmpty()) {
                builder.lore("&bNo potion effects");
            } else {
                List<String> lore = new ArrayList<>();

                effects.forEach(effect -> {
                    String name = PotionUtil.getName(effect.getType()) + " " + (effect.getAmplifier() + 1);
                    String duration = " (" + TimeUtil.millisToTimer((effect.getDuration() / 20) * 1000L) + ")";
                    lore.add("&b" + name + "&f" + duration);
                });

                builder.lore(lore, player);
            }

            return builder.build();
        }

    }

    @AllArgsConstructor
    private class PotionsButton extends Button {

        private String name;
        private int potions;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.POTION)
                    .durability(16421)
                    .amount(potions == 0 ? 1 : potions, false)
                    .name("&bPotions")
                    .lore("&b" + name + " &fhad &b" + potions + " &fpotion" + (potions == 1 ? "" : "s") + " left.")
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private class StatisticsButton extends Button {

        private MatchSnapshot snapshot;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .name("&bMatch Stats")
                    .lore(Arrays.asList(
                            "&7• &bHits: &f" + snapshot.getTotalHits(),
                            "&7• &bLongest Combo: &f" + snapshot.getLongestCombo(),
                            "",
                            "&bPotions: ",
                            "&7• &bPotions Thrown: &f" + snapshot.getPotionsThrown(),
                            "&7• &bPotions Missed: &f" + snapshot.getPotionsMissed(),
                            "&7• &bPotion Accuracy: &f" + snapshot.getPotionAccuracy()
                    ), player)
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private class SwitchInventoryButton extends Button {

        private UUID opponent;

        @Override
        public ItemStack getButtonItem(Player player) {
            MatchSnapshot snapshot = MatchSnapshot.getByUuid(opponent);

            if (snapshot != null) {
                return new ItemBuilder(Material.LEVER)
                        .name("&7Press to switch to " + Bukkit.getPlayer(opponent).getName() + " inventory.")
                        .lore("&aClick to Switch")
                        .clearFlags()
                        .build();
            } else {
                return new ItemStack(Material.AIR);
            }
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            player.chat("/viewinv " + snapshot.getOpponent().toString());
        }
    }

    @AllArgsConstructor
    private class HungerButton extends Button {

        private int hunger;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.COOKED_BEEF)
                    .name("&bHunger: &f" + hunger + "/20")
                    .amount(hunger == 0 ? 1 : hunger, false)
                    .clearFlags()
                    .build();
        }

    }

}
