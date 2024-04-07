package me.lrxh.practice.match.menu;

import lombok.AllArgsConstructor;
import me.lrxh.practice.util.*;
import me.lrxh.practice.util.menu.Button;
import me.lrxh.practice.util.menu.Menu;
import me.lrxh.practice.util.menu.button.DisplayButton;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class ViewInventoryMenu extends Menu {

    private final Player target;

    public ViewInventoryMenu(Player target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + target.getName() + "'s Inventory";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        if (player == null) {
            return buttons;
        }

        ItemStack[] fixedContents = InventoryUtil.fixInventoryOrder(this.target.getInventory().getContents());

        for (int i = 0; i < fixedContents.length; ++i) {
            ItemStack itemStack = fixedContents[i];

            if (itemStack != null) {
                if (itemStack.getType() != Material.AIR) {
                    buttons.put(i, new DisplayButton(itemStack, true));
                }
            }
        }

        for (int i = 0; i < target.getInventory().getArmorContents().length; ++i) {
            ItemStack itemStack = target.getInventory().getArmorContents()[i];

            if (itemStack != null && itemStack.getType() != Material.AIR) {
                buttons.put(39 - i, new DisplayButton(itemStack, true));
            }
        }

        int pos = 45;
        buttons.put(pos++, new HealthButton(target.getHealth() == 0.0 ? 0 : (int) Math.round(target.getHealth() / 2.0)));
        buttons.put(pos++, new HungerButton(target.getFoodLevel()));
        buttons.put(pos, new EffectsButton(target.getActivePotionEffects()));
        return buttons;
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

    @AllArgsConstructor
    private static class HealthButton extends Button {

        private int health;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.MELON)
                    .name("&eHealth: &d" + health + "/10 " + StringEscapeUtils.unescapeJava("\u2764"))
                    .amount(health == 0 ? 1 : health, false)
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private static class HungerButton extends Button {

        private int hunger;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.COOKED_BEEF)
                    .name("&eHunger: &d" + hunger + "/20")
                    .amount(hunger == 0 ? 1 : hunger, false)
                    .clearFlags()
                    .build();
        }

    }

    @AllArgsConstructor
    private static class EffectsButton extends Button {

        private Collection<PotionEffect> effects;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.POTION).name("&ePotion Effects");

            if (effects.isEmpty()) {
                builder.lore(CC.GRAY + "No effects");
            } else {
                List<String> lore = new ArrayList<>();

                effects.forEach(effect -> {
                    String name = PotionUtil.getName(effect.getType()) + " " + (effect.getAmplifier() + 1);
                    String duration = " (" + TimeUtil.millisToTimer(effect.getDuration() / 20 * 1000L) + ")";
                    lore.add(CC.PINK + name + CC.GRAY + duration);
                });

                builder.lore(lore, player);
            }

            return builder.build();
        }

    }

}
