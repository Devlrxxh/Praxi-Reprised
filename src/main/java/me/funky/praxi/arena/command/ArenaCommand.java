package me.funky.praxi.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.arena.ArenaType;
import me.funky.praxi.arena.generator.ArenaGenerator;
import me.funky.praxi.arena.generator.Schematic;
import me.funky.praxi.arena.impl.SharedArena;
import me.funky.praxi.arena.impl.StandaloneArena;
import me.funky.praxi.arena.selection.Selection;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ChatComponentBuilder;
import me.funky.praxi.util.ChatHelper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Objects;

@CommandAlias("arena")
@CommandPermission("praxi.admin.arena")
@Description("Command to manage and create arenas.")
public class ArenaCommand extends BaseCommand {
    @Default
    @Subcommand("help")
    public void help(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&cArena Management &7[&f1/3&7] - &f/arena help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/arena create &7<value> - &fCreate arena"));
        player.sendMessage(CC.translate("&7* &c/arena remove &7<arena> - &fRemove arena"));
        player.sendMessage(CC.translate("&7* &c/arena save &7- &fSave arenas to file"));
        player.sendMessage(CC.translate("&7* &c/arena selection &7- &fGet Selection wand"));
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("help 1")
    public void help1(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&cArena Management &7[&f1/3&7] - &f/arena help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/arena create &7<value> - &fCreate arena"));
        player.sendMessage(CC.translate("&7* &c/arena remove &7<arena> - &fRemove arena"));
        player.sendMessage(CC.translate("&7* &c/arena save &7- &fSave arenas to file"));
        player.sendMessage(CC.translate("&7* &c/arena selection &7- &fGet Selection wand"));
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("help 2")
    public void help2(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&cArena Management &7[&f2/3&7] - &f/arena help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/arena status &7<arena> - &fSee arena status"));
        player.sendMessage(CC.translate("&7* &c/arena genhelper &7- &fPlace Generator Helper"));
        player.sendMessage(CC.translate("&7* &c/arena addKit &7<arena> &7<kit> &7- &fAdd arena to kit"));
        player.sendMessage(CC.translate("&7* &c/arena removeKit &7<arena> &7<kit> &7- &fRemove arena to kit"));
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }

    @Subcommand("help 3")
    public void help3(Player player) {
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
        player.sendMessage(CC.translate("&cArena Management &7[&f3/3&7] - &f/arena help <page>"));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7* &c/arena generate &7- &fArena Generator"));
        player.sendMessage(CC.translate("&7* &c/arena list &7- &fList all arenas"));
        player.sendMessage(CC.translate("&7* &c/arena setspawn &7<arena> &7<a/b> &7- &fSet arena spawns"));
        player.sendMessage(CC.translate("&7&m-----------------------------------------"));
    }


    @Subcommand("create")
    @Syntax("<arena>")
    public void create(Player player, String arenaName) {
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

    @Subcommand("remove")
    @CommandCompletion("@arenas")
    @Syntax("<arena>")
    public void remove(Player player, String arenaName) {
        if (checkArena(arenaName)) {
            player.sendMessage(CC.translate("&4ERROR - &cArena doesn't exists!"));
            return;
        }
        Arena arena = Arena.getByName(arenaName);
        if (arena != null) {
            arena.delete();

            player.sendMessage(CC.GOLD + "Deleted arena \"" + arena.getName() + "\"");
        } else {
            player.sendMessage(CC.RED + "An arena with that name does not exist.");
        }
    }

    @Subcommand("selection")
    public void selection(Player player) {
        if (player.getInventory().first(Selection.SELECTION_WAND) != -1) {
            player.getInventory().remove(Selection.SELECTION_WAND);
        } else {
            player.getInventory().addItem(Selection.SELECTION_WAND);
        }
        player.sendMessage(CC.translate("&aSuccessfully given selection wand!"));
        player.updateInventory();
    }

    @Subcommand("save")
    public void save(Player player) {
        for (Arena arena : Arena.getArenas()) {
            arena.save();
        }

        player.sendMessage(ChatColor.GREEN + "Saved all arenas!");
    }

    @Subcommand("status")
    @CommandCompletion("@arenas")
    @Syntax("<arena>")
    public void status(Player player, String arenaName) {
        if (checkArena(arenaName)) {
            player.sendMessage(CC.translate("&4ERROR - &cArena doesn't exists!"));
            return;
        }
        Arena arena = Arena.getByName(arenaName);
        if (arena != null) {
            player.sendMessage(CC.GOLD + CC.BOLD + "Arena Status " + CC.GRAY + "(" +
                    (arena.isSetup() ? CC.GREEN : CC.RED) + arena.getName() + CC.GRAY + ")");

            player.sendMessage(CC.GREEN + "Cuboid Lower Location: " + CC.YELLOW +
                    (arena.getLowerCorner() == null ?
                            StringEscapeUtils.unescapeJava("✗") :
                            StringEscapeUtils.unescapeJava("✓")));

            player.sendMessage(CC.GREEN + "Cuboid Upper Location: " + CC.YELLOW +
                    (arena.getUpperCorner() == null ?
                            StringEscapeUtils.unescapeJava("✗") :
                            StringEscapeUtils.unescapeJava("✓")));

            player.sendMessage(CC.GREEN + "Spawn A Location: " + CC.YELLOW +
                    (arena.getSpawnA() == null ?
                            StringEscapeUtils.unescapeJava("✗") :
                            StringEscapeUtils.unescapeJava("✓")));

            player.sendMessage(CC.GREEN + "Spawn B Location: " + CC.YELLOW +
                    (arena.getSpawnB() == null ?
                            StringEscapeUtils.unescapeJava("✗") :
                            StringEscapeUtils.unescapeJava("✓")));

            player.sendMessage(CC.GREEN + "Kits: " + CC.YELLOW + StringUtils.join(arena.getKits(), ", "));
        } else {
            player.sendMessage(CC.RED + "An arena with that name does not exist.");
        }
    }

    @Subcommand("genhelper")
    public void genhelper(Player player) {
        Block origin = player.getLocation().getBlock();
        Block up = origin.getRelative(BlockFace.UP);

        origin.setType(Material.SPONGE);
        up.setType(Material.SIGN_POST);

        if (up.getState() instanceof Sign) {
            Sign sign = (Sign) up.getState();
            sign.setLine(0, ((int) player.getLocation().getPitch()) + "");
            sign.setLine(1, ((int) player.getLocation().getYaw()) + "");
            sign.update();

            player.sendMessage(CC.GREEN + "Generator helper placed.");
        }
    }

    @Subcommand("addKit")
    @CommandCompletion("@arenas @kits")
    @Syntax("<arena> <kits>")
    public void addKit(Player player, String arenaName, String kitName) {
        if (checkArena(arenaName)) {
            player.sendMessage(CC.translate("&4ERROR - &cArena doesn't exists!"));
            return;
        }
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Arena arena = Arena.getByName(arenaName);
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;
        if (arena == null) return;

        arena.getKits().add(kit.getName());
        arena.save();

        player.sendMessage(ChatColor.GOLD + "Added kit \"" + kit.getName() +
                "\" to arena \"" + arena.getName() + "\"");
    }

    @Subcommand("removeKit")
    @CommandCompletion("@arenas @kits")
    @Syntax("<arena> <kits>")
    public void removeKit(Player player, String arenaName, String kitName) {
        if (checkArena(arenaName)) {
            player.sendMessage(CC.translate("&4ERROR - &cArena doesn't exists!"));
            return;
        }
        if (!Kit.getKits().contains(Kit.getByName(kitName))) {
            player.sendMessage(CC.translate("&4ERROR - &cKit doesn't exists!"));
            return;
        }
        Arena arena = Arena.getByName(arenaName);
        Kit kit = Kit.getByName(kitName);
        if (kit == null) return;
        if (arena == null) return;

        arena.getKits().remove(kit.getName());
        arena.save();

        player.sendMessage(ChatColor.GOLD + "Removed kit \"" + kit.getName() +
                "\" from arena \"" + arena.getName() + "\"");
    }

    @Subcommand("generate")
    public void generate(Player player) {
        File schematicsFolder = new File(Praxi.getInstance().getDataFolder().getPath() + File.separator + "schematics");

        if (!schematicsFolder.exists()) {
            player.sendMessage(CC.RED + "The schematics folder does not exist.");
            return;
        }

        for (File file : Objects.requireNonNull(schematicsFolder.listFiles())) {
            if (!file.isDirectory() && file.getName().contains(".schematic")) {
                boolean duplicate = file.getName().endsWith("_duplicate.schematic");

                String name = file.getName()
                        .replace(".schematic", "")
                        .replace("_duplicate", "");

                Arena parent = Arena.getByName(name);

                if (parent != null) {
                    if (!(parent instanceof StandaloneArena)) {
                        System.out.println("Skipping " + name + " because it's not duplicate and an arena with that name already exists.");
                        continue;
                    }
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            new ArenaGenerator(name, Bukkit.getWorlds().get(0), new Schematic(file), duplicate ?
                                    (parent != null ? ArenaType.DUPLICATE : ArenaType.STANDALONE) : ArenaType.SHARED)
                                    .generate(file, (StandaloneArena) parent);
                        } catch (Exception ignored) {
                        }
                    }
                }.runTask(Praxi.getInstance());
            }
        }

        player.sendMessage(CC.GREEN + "Generating arenas... See console for details.");
    }

    @Subcommand("list")
    public void list(Player player) {
        player.sendMessage(CC.GOLD + "Arenas:");

        if (Arena.getArenas().isEmpty()) {
            player.sendMessage(CC.GRAY + "There are no arenas.");
            return;
        }

        for (Arena arena : Arena.getArenas()) {
            if (arena.getType() != ArenaType.DUPLICATE) {
                ChatComponentBuilder builder = new ChatComponentBuilder("")
                        .parse("&7- " + (arena.isSetup() ? "&a" : "&c") + arena.getName() +
                                "&7(" + arena.getType().name() + ") ");

                ChatComponentBuilder status = new ChatComponentBuilder("").parse("&7[&6STATUS&7]");
                status.attachToEachPart(ChatHelper.hover("&6Click to view this arena's status."));
                status.attachToEachPart(ChatHelper.click("/arena status " + arena.getName()));

                builder.append(" ");

                for (BaseComponent component : status.create()) {
                    builder.append((TextComponent) component);
                }

                player.spigot().sendMessage(builder.create());
            }
        }
    }

    @Subcommand("setspawn")
    @CommandCompletion("@arenas")
    @Syntax("<arena> <pos>")
    public void setspawn(Player player, String arenaName, String pos) {
        if (checkArena(arenaName)) {
            player.sendMessage(CC.translate("&4ERROR - &cArena doesn't exists!"));
            return;
        }
        Arena arena = Arena.getByName(arenaName);
        if (arena != null) {
            if (pos.equalsIgnoreCase("a")) {
                arena.setSpawnA(player.getLocation());
            } else if (pos.equalsIgnoreCase("b")) {
                arena.setSpawnB(player.getLocation());
            } else {
                player.sendMessage(CC.RED + "Invalid spawn point. Try \"a\" or \"b\".");
                return;
            }

            arena.save();

            player.sendMessage(CC.GOLD + "Updated spawn point \"" + pos + "\" for arena \"" + arena.getName() + "\"");
        } else {
            player.sendMessage(CC.RED + "An arena with that name already exists.");
        }
    }

    private boolean checkArena(String arena) {
        return !Arena.getArenas().contains(Arena.getByName(arena));
    }
}
