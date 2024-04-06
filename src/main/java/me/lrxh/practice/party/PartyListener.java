package me.lrxh.practice.party;

import me.lrxh.practice.profile.Profile;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (event.getMessage().startsWith("@") || event.getMessage().startsWith("!")) {
            if (profile.getParty() != null) {
                event.setCancelled(true);
                profile.getParty().sendChat(event.getPlayer(), ChatColor.stripColor(event.getMessage().substring(1)));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());

        if (profile != null) {
            if (profile.getParty() != null) {
                if (profile.getParty().getLeader().equals(event.getPlayer())) {
                    profile.getParty().disband();
                } else {
                    profile.getParty().leave(event.getPlayer(), false);
                }
            }
        }
    }

}
