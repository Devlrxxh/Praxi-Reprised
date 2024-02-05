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
        player.sendMessage(CC.translate("&cKit Management &7[&f1/2&7] - &f/kit help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/kit create &7<value> - &fCreate kit"));
        player.sendMessage(CC.translate("&7* &c/kit getinv &7<kit> - &fGet kit loadout"));
        player.sendMessage(CC.translate("&7* &c/kit list &7- &fList all kits"));
        player.sendMessage(CC.translate("&7* &c/arena setinv &7<kit> &7- &fSet kit loadout"));
        player.sendMessage(CC.translate("&7* &c/arena setkb &7<kit> &7<kb> &7- &fSet kit KB Profile"));
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("help 1")
    public void help1(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&cKit Management &7[&f1/2&7] - &f/kit help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/kit create &7<value> - &fCreate kit"));
        player.sendMessage(CC.translate("&7* &c/kit getinv &7<kit> - &fGet kit loadout"));
        player.sendMessage(CC.translate("&7* &c/kit list &7- &fList all kits"));
        player.sendMessage(CC.translate("&7* &c/arena setinv &7<kit> &7- &fSet kit loadout"));
        player.sendMessage(CC.translate("&7* &c/arena setkb &7<kit> &7<kb> &7- &fSet kit KB Profile"));
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("help 2")
    public void help2(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&cKit Management &7[&f1/2&7] - &f/kit help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/kit seticon &7<kit> - &fSet kit icon"));
        player.sendMessage(CC.translate("&7* &c/kit enable &7<kit> - &fEnable/Disable kit"));
        player.sendMessage(CC.translate("&7* &c/kit setdescription &7<kit> &7<value> &7- &fSet kit description"));
        player.sendMessage(CC.translate("&7* &c/kit build &7<kit> &7- &fAdd Build rule"));
        player.sendMessage(CC.translate("&7* &c/kit spleef &7<kit> &7- &fAdd Spleef rule"));
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("help 3")
    public void help3(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&cKit Management &7[&f1/3&7] - &f/kit help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/kit sumo &7<kit> - &fAdd Sumo rule"));
        player.sendMessage(CC.translate("&7* &c/kit hpregen &7<kit> - &fSet HPRegen rule"));
        player.sendMessage(CC.translate("&7* &c/kit hitdelay &7<kit> <value> - &fSet hit delay"));
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

    @Subcommand("seticon")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void setIcon(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.setDisplayIcon(player.getItemInHand());
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's icon.");
    }

    @Subcommand("enable")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void enable(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.setEnabled(!kit.isEnabled());
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's status to " + (kit.isEnabled() ? "Enabled" : ChatColor.RED + "Disabled" + "."));
    }

    @Subcommand("setdescription")
    @CommandCompletion("@kits")
    @Syntax("<kit> <description>")
    public void setdescription(Player player, String kitName, String description) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.setDescription(description);
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's description.");
    }

    @Subcommand("setdescription")
    @CommandCompletion("@kits")
    @Syntax("<kit> <description>")
    public void setdescription2(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.setDescription("none");
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's description.");
    }

    @Subcommand("build")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void build(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.getGameRules().setBuild(!kit.getGameRules().isBuild());
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's build status to " + (kit.getGameRules().isBuild() ? "Enabled" : ChatColor.RED + "Disabled" + "."));
    }

    @Subcommand("spleef")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void spleef(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.getGameRules().setSpleef(!kit.getGameRules().isSpleef());
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's spleef status to " + (kit.getGameRules().isSpleef() ? "Enabled" : ChatColor.RED + "Disabled" + "."));
    }

    @Subcommand("sumo")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void sumo(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.getGameRules().setSumo(!kit.getGameRules().isSumo());
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's sumo status to " + (kit.getGameRules().isSumo() ? "Enabled" : ChatColor.RED + "Disabled" + "."));
    }

    @Subcommand("hpregen")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void hpregen(Player player, String kitName) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.getGameRules().setHealthRegeneration(!kit.getGameRules().isHealthRegeneration());
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's health regeneration status to " + (kit.getGameRules().isShowHealth() ? "Enabled" : ChatColor.RED + "Disabled" + "."));
    }

    @Subcommand("hitdelay")
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    public void showhp(Player player, String kitName, int delay) {
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;

        kit.getGameRules().setHitDelay(delay);
        kit.save();

        player.sendMessage(CC.GREEN + "You updated the kit's hit delay to &f" + delay + "!");
    }
}
