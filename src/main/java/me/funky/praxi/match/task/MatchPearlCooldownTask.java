package me.funky.praxi.match.task;

import me.funky.praxi.Locale;
import me.funky.praxi.Praxi;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchPearlCooldownTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Praxi.getInstance().getServer().getOnlinePlayers()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getState() == ProfileState.FIGHTING || profile.getState() == ProfileState.EVENT) {
                if (profile.getEnderpearlCooldown().hasExpired()) {
                    if (!profile.getEnderpearlCooldown().isNotified()) {
                        profile.getEnderpearlCooldown().setNotified(true);
                        player.sendMessage(Locale.MATCH_ENDERPEARL_COOLDOWN_EXPIRED.format());
                    }
                } else {
                    int seconds = Math.round(profile.getEnderpearlCooldown().getRemaining()) / 1_000;

                    player.setLevel(seconds);
                    player.setExp(profile.getEnderpearlCooldown().getRemaining() / 16_000.0F);
                }
            } else {
                if (player.getLevel() > 0) {
                    player.setLevel(0);
                }

                if (player.getExp() > 0.0F) {
                    player.setExp(0.0F);
                }
            }
        }
    }

}
