package me.funky.praxi.queue;

import me.funky.praxi.Locale;
import me.funky.praxi.Praxi;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.profile.hotbar.Hotbar;
import me.funky.praxi.profile.hotbar.HotbarItem;
import me.funky.praxi.profile.meta.ProfileKitData;
import me.funky.praxi.queue.menu.QueueSelectKitMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class QueueListener implements Listener {


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            HotbarItem hotbarItem = Hotbar.fromItemStack(event.getItem());

            if (hotbarItem != null) {
                Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
                boolean cancelled = true;

                if (hotbarItem == HotbarItem.QUEUE_JOIN_RANKED) {
                    if(profile.getWins() < Praxi.getInstance().getMainConfig().getInteger("RANKED.REQUIRED-WINS")){
                        event.getPlayer().sendMessage(Locale.RANKED_ERROR.format(Praxi.getInstance().getMainConfig().getInteger("RANKED.REQUIRED-WINS") - profile.getWins()));
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
