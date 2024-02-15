package me.funky.praxi.match.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Locale;
import me.funky.praxi.match.MatchSnapshot;
import me.funky.praxi.util.InventoryUtil;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.PotionUtil;
import me.funky.praxi.util.TimeUtil;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.menu.button.DisplayButton;
import me.funky.praxi.util.menu.filters.Filters;
import org.apache.commons.lang.StringEscapeUtils;
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
        return "&7Inventory of " + snapshot.getUsername();
    }

    @Override
    public Filters getFilter() {
        return Filters.FILL;
    }

    public boolean getFixedPositions() {
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


        int pos = 46;

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

        buttons.put(46, new SwitchInventoryButton(this.snapshot.getUuid()));

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        player.sendMessage(Locale.VIEWING_INVENTORY.format(player, snapshot.getUsername()));
    }

    @AllArgsConstructor
    private static class HealthButton extends Button {

        private double health;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.MELON)
                    .name("&bHealth: &f" + health + "/10 &4" + StringEscapeUtils.unescapeJava("❤"))
                    .amount((int) (health == 0 ? 1 : health))
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private static class EffectsButton extends Button {

        private Collection<PotionEffect> effects;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.POTION).name("&bPotion Effects");

            if (effects.isEmpty()) {
                builder.lore("&bNo potion effects");
            } else {
                List<String> lore = new ArrayList<>();

                effects.forEach(effect -> {
                    String name = PotionUtil.getName(effect.getType()) + " " + (effect.getAmplifier() + 1);
                    String duration = " (" + TimeUtil.millisToTimer((effect.getDuration() / 20) * 1000L) + ")";
                    lore.add("&b" + name + "&f" + duration);
                });

                builder.lore(lore);
            }

            return builder.build();
        }

    }

    @AllArgsConstructor
    private static class PotionsButton extends Button {

        private String name;
        private int potions;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.POTION)
                    .durability(16421)
                    .amount(potions == 0 ? 1 : potions)
                    .name("&bPotions")
                    .lore("&b" + name + " &fhad &b" + potions + " &fpotion" + (potions == 1 ? "" : "s") + " left.")
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private static class StatisticsButton extends Button {

        private MatchSnapshot snapshot;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .name("&bMatch Stats")
                    .lore(Arrays.asList(
                            "&bHits: &f" + snapshot.getTotalHits(),
                            "&bLongest Combo: &f" + snapshot.getLongestCombo(),
                            "&bPotions Thrown: &f" + snapshot.getPotionsThrown(),
                            "&bPotions Missed: &f" + snapshot.getPotionsMissed(),
                            "&bPotion Accuracy: &f" + snapshot.getPotionAccuracy()
                    ))
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
                return new ItemBuilder(Material.ARROW)
                        .name("&7Opponent's Inventory")
                        .lore("&fSwitch to &b" + snapshot.getUsername() + "&f's inventory")
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
                    .amount(hunger == 0 ? 1 : hunger)
                    .clearFlags()
                    .build();
        }

    }

}
