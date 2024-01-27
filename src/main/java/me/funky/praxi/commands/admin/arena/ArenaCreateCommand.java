package me.funky.praxi.commands.admin.arena;

import me.funky.praxi.arena.Arena;
import me.funky.praxi.arena.impl.SharedArena;
import me.funky.praxi.arena.selection.Selection;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = "arena create", permission = "praxi.admin.arena")
public class ArenaCreateCommand {

    public void execute(Player player, String arenaName) {
        if (Arena.getByName(arenaName) == null) {
            Selection selection = Selection.createOrGetSelection(player);

            if (selection.isFullObject()) {
                Arena arena = new SharedArena(arenaName, selection.getPoint1(), selection.getPoint2());
                Arena.getArenas().add(arena);

                player.sendMessage(CC.GOLD + "Created new arena \"" + arenaName + "\"");
            } else {
                player.sendMessage(CC.RED + "Your selection is incomplete.");
            }
        } else {
            player.sendMessage(CC.RED + "An arena with that name already exists.");
        }
    }

}
