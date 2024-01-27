package me.funky.praxi.commands.admin.kits;

import me.funky.praxi.kit.Kit;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = "kit setloadout", permission = "praxi.kit.setloadout")
public class KitSetLoadoutCommand {

    public void execute(Player player, Kit kit) {
        if (kit == null) {
            player.sendMessage(CC.RED + "A kit with that name does not exist.");
            return;
        }

        kit.getKitLoadout().setArmor(player.getInventory().getArmorContents());
        kit.getKitLoadout().setContents(player.getInventory().getContents());
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's loadout.");
    }

}
