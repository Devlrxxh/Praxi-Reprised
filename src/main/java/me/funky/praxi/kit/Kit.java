package me.funky.praxi.kit;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.Praxi;
import me.funky.praxi.kit.meta.KitEditRules;
import me.funky.praxi.kit.meta.KitGameRules;
import me.funky.praxi.queue.Queue;
import me.funky.praxi.util.InventoryUtil;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.config.BasicConfigurationFile;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Kit {

    @Getter
    private static final List<Kit> kits = new ArrayList<>();

    @Getter
    private final String name;
    @Getter
    private final KitLoadout kitLoadout = new KitLoadout();
    @Getter
    private final KitEditRules editRules = new KitEditRules();
    @Getter
    private final KitGameRules gameRules = new KitGameRules();
    @Getter
    @Setter
    private boolean enabled;
    @Getter
    @Setter
    private String knockbackProfile;
    @Setter
    private ItemStack displayIcon;

    public Kit(String name) {
        this.name = name;
        this.displayIcon = new ItemStack(Material.DIAMOND_SWORD);
    }

    public static void init() {
        FileConfiguration config = Praxi.getInstance().getKitsConfig().getConfiguration();

        for (String key : config.getConfigurationSection("kits").getKeys(false)) {
            String path = "kits." + key;

            Kit kit = new Kit(key);
            kit.setEnabled(config.getBoolean(path + ".enabled"));

            if (config.contains(path + ".knockback-profile")) {
                kit.setKnockbackProfile(config.getString(path + ".knockback-profile"));
            }

            kit.setDisplayIcon(new ItemBuilder(Material.valueOf(config.getString(path + ".icon.material")))
                    .durability(config.getInt(path + ".icon.durability"))
                    .build());

            if (config.contains(path + ".loadout.armor")) {
                kit.getKitLoadout().setArmor(InventoryUtil.deserializeInventory(config.getString(path + ".loadout.armor")));
            }

            if (config.contains(path + ".loadout.contents")) {
                kit.getKitLoadout().setContents(InventoryUtil.deserializeInventory(config.getString(path + ".loadout.contents")));
            }

            kit.getGameRules().setBuild(config.getBoolean(path + ".game-rules.allow-build"));
            kit.getGameRules().setSpleef(config.getBoolean(path + ".game-rules.spleef"));
            kit.getGameRules().setParkour(config.getBoolean(path + ".game-rules.parkour"));
            kit.getGameRules().setSumo(config.getBoolean(path + ".game-rules.sumo"));
            kit.getGameRules().setHealthRegeneration(config.getBoolean(path + ".game-rules.health-regeneration"));
            kit.getGameRules().setShowHealth(config.getBoolean(path + ".game-rules.show-health"));
            kit.getGameRules().setHitDelay(config.getInt(path + ".game-rules.hit-delay"));
            kit.getEditRules().setAllowPotionFill(config.getBoolean(".edit-rules.allow-potion-fill"));

            if (config.getConfigurationSection(path + ".edit-rules.items") != null) {
                for (String itemKey : config.getConfigurationSection(path + ".edit-rules.items")
                        .getKeys(false)) {
                    kit.getEditRules().getEditorItems().add(new ItemBuilder(Material.valueOf(
                            config.getString(path + ".edit-rules.items." + itemKey + ".material")))
                            .durability(config.getInt(path + ".edit-rules.items." + itemKey + ".durability"))
                            .amount(config.getInt(path + ".edit-rules.items." + itemKey + ".amount"))
                            .build());
                }
            }

            kits.add(kit);
        }

        kits.forEach(kit -> {
            if (kit.isEnabled()) {
                new Queue(kit, false);
                new Queue(kit, true);
            }
        });
    }

    public static Kit getByName(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }

        return null;
    }

    public ItemStack getDisplayIcon() {
        return this.displayIcon.clone();
    }

    public void save() {
        String path = "kits." + name;

        BasicConfigurationFile configFile = Praxi.getInstance().getKitsConfig();
        configFile.getConfiguration().set(path + ".enabled", enabled);
        configFile.getConfiguration().set(path + ".icon.material", displayIcon.getType().name());
        configFile.getConfiguration().set(path + ".icon.durability", displayIcon.getDurability());
        configFile.getConfiguration().set(path + ".loadout.armor", InventoryUtil.serializeInventory(kitLoadout.getArmor()));
        configFile.getConfiguration().set(path + ".loadout.contents", InventoryUtil.serializeInventory(kitLoadout.getContents()));
        configFile.getConfiguration().set(path + ".game-rules.allow-build", gameRules.isBuild());
        configFile.getConfiguration().set(path + ".game-rules.spleef", gameRules.isSpleef());
        configFile.getConfiguration().set(path + ".game-rules.parkour", gameRules.isParkour());
        configFile.getConfiguration().set(path + ".game-rules.sumo", gameRules.isSumo());
        configFile.getConfiguration().set(path + ".game-rules.health-regeneration", gameRules.isHealthRegeneration());
        configFile.getConfiguration().set(path + ".game-rules.show-health", gameRules.isShowHealth());
        configFile.getConfiguration().set(path + ".game-rules.hit-delay", gameRules.getHitDelay());
        configFile.getConfiguration().set(path + ".edit-rules.allow-potion-fill", editRules.isAllowPotionFill());

        if (knockbackProfile != null) {
            configFile.getConfiguration().set(path + ".knockback-profile", knockbackProfile);
        }

        try {
            configFile.getConfiguration().save(configFile.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
