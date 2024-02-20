package me.lrxh.practice.duel;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DuelRequest {

    @Getter
    private final UUID sender;
    @Getter
    private final UUID target;
    @Getter
    private final boolean party;
    private final long timestamp = System.currentTimeMillis();
    @Getter
    @Setter
    private Kit kit;
    @Getter
    @Setter
    private Arena arena;

    DuelRequest(UUID sender, UUID target, boolean party) {
        this.sender = sender;
        this.target = target;
        this.party = party;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.timestamp >= 30_000;
    }

    public void expire() {
        Player sender = Bukkit.getPlayer(this.sender);
        Player target = Bukkit.getPlayer(this.target);

        if (sender != null && target != null) {
            sender.sendMessage(ChatColor.RED + "Your " + kit.getName() + " duel request to " +
                    target.getName() + " has expired!");

            target.sendMessage(ChatColor.RED + "The " + kit.getName() + " duel request sent by " +
                    sender.getName() + " has expired!");
        }
    }

}
