package me.lrxh.practice.profile;

import lombok.Getter;
import me.lrxh.practice.Practice;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

@Getter
public enum KillEffects {
    NONE("None") {
        @Override
        public void execute(Player player, Location location) {
        }
    },
    LIGHTNING("Lightning") {
        @Override
        public void execute(Player player, Location location) {
            double x = location.getX();
            double y = location.getY() + 2.0;
            double z = location.getZ();
            Location lightningLocation = new Location(location.getWorld(), x, y, z);
            location.getWorld().strikeLightning(lightningLocation);
        }
    },
    FIREWORKS("Fireworks") {
        @Override
        public void execute(Player player, Location location) {
            Firework firework = location.getWorld().spawn(location, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            FireworkEffect.Builder builder = FireworkEffect.builder()
                    .withColor(Color.RED)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .trail(true)
                    .flicker(false);
            meta.addEffect(builder.build());
            meta.setPower(1);
            firework.setFireworkMeta(meta);
            Bukkit.getScheduler().runTaskLater(Practice.getInstance(), firework::detonate, 5L);
        }
    };

    private final String displayName;

    KillEffects(String displayName) {
        this.displayName = displayName;
    }

    public abstract void execute(Player player, Location location);
}
