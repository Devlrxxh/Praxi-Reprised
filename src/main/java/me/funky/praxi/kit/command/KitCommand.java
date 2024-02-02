package me.funky.praxi.kit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.util.CC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("kit")
@CommandPermission("praxi.admin.kit")
@Description("Command to manage and create kits.")
public class KitCommand extends BaseCommand {
    @Default
    @Subcommand("help")
    public void help(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&cKit Management &7[&f1/1&7] - &f/kit help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/kit create &7<value> - &fCreate kit"));
        player.sendMessage(CC.translate("&7* &c/kit getinv &7<kit> - &fGet kit loadout"));
        player.sendMessage(CC.translate("&7* &c/kit list &7- &fList all kits"));
        player.sendMessage(CC.translate("&7* &c/arena setinv &7<kit> &7- &fSet kit loadout"));
        player.sendMessage(CC.translate("&7* &c/arena setkb &7<kit> &7<kb> &7- &fSet kit KB Profile"));
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("create")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void create(Player player, String kitName) {
        if (Kit.getByName(kitName) != null) {
            player.sendMessage(CC.RED + "A kit with that name already exists.");
            return;
        }

        Kit kit = new Kit(kitName);
        kit.save();

        Kit.getKits().add(kit);

        player.sendMessage(CC.GREEN + "You created a new kit.");
    }

    @Subcommand("getinv")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void getinv(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        player.getInventory().setArmorContents(kit.getKitLoadout().getArmor());
        player.getInventory().setContents(kit.getKitLoadout().getContents());
        player.updateInventory();

        player.sendMessage(CC.GREEN + "You received the kit's loadout.");
    }

    @Subcommand("list")
    public void list(Player player) {
        player.sendMessage(ChatColor.GOLD + "Kits");

        for (Kit kit : Kit.getKits()) {
            player.sendMessage(kit.getName());
        }
    }

    @Subcommand("setinv")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void setinv(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.getKitLoadout().setArmor(player.getInventory().getArmorContents());
        kit.getKitLoadout().setContents(player.getInventory().getContents());
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's loadout.");
    }

    @Subcommand("setkb")
    @CommandCompletion("@kits")
    @Syntax("<kit> <kb>")
    public void setkb(Player player, String kitName, String kbName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.setKnockbackProfile(kbName);
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's kb.");
    }
}
