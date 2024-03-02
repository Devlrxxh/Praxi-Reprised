package me.lrxh.practice.queue;

import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileState;
import me.lrxh.practice.profile.hotbar.HotbarItem;
import me.lrxh.practice.queue.menu.QueueSelectKitMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class QueueListener implements Listener {


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            HotbarItem hotbarItem = Practice.getInstance().getHotbar().fromItemStack(event.getItem());

            if (hotbarItem != null) {
                Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
                boolean cancelled = true;

                if (hotbarItem == HotbarItem.QUEUE_JOIN_RANKED) {
                    if (profile.getWins() < Practice.getInstance().getMainConfig().getInteger("RANKED.REQUIRED-WINS")) {
                        event.getPlayer().sendMessage(Locale.RANKED_ERROR.format(event.getPlayer(), Practice.getInstance().getMainConfig().getInteger("RANKED.REQUIRED-WINS") - profile.getWins()));
                        return;
                    }
                    if (!profile.isBusy()) {
                        new QueueSelectKitMenu(true).openMenu(event.getPlayer());
                    }
                } else if (hotbarItem == HotbarItem.QUEUE_JOIN_UNRANKED) {
                    if (!profile.isBusy()) {
                        new QueueSelectKitMenu(false).openMenu(event.getPlayer());
                    }
                } else if (hotbarItem == HotbarItem.QUEUE_LEAVE) {
                    if (profile.getState() == ProfileState.QUEUEING) {
                        profile.getQueueProfile().getQueue().removePlayer(profile.getQueueProfile());
                    }
                } else {
                    cancelled = false;
                }

                event.setCancelled(cancelled);
            }
        }
    }

}
