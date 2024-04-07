package me.lrxh.practice;


import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import me.lrxh.practice.arena.Arena;
import me.lrxh.practice.arena.ArenaListener;
import me.lrxh.practice.arena.command.ArenaCommand;
import me.lrxh.practice.commands.admin.general.FollowCommand;
import me.lrxh.practice.commands.admin.general.MainCommand;
import me.lrxh.practice.commands.admin.general.SilentCommand;
import me.lrxh.practice.commands.donater.FlyCommand;
import me.lrxh.practice.commands.user.EloCommand;
import me.lrxh.practice.commands.user.PingCommand;
import me.lrxh.practice.commands.user.StatsCommand;
import me.lrxh.practice.commands.user.duels.DuelCommand;
import me.lrxh.practice.commands.user.duels.RematchCommand;
import me.lrxh.practice.commands.user.match.SpectateCommand;
import me.lrxh.practice.commands.user.match.ViewInventoryCommand;
import me.lrxh.practice.commands.user.party.PartyCommand;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.kit.KitEditorListener;
import me.lrxh.practice.kit.command.KitCommand;
import me.lrxh.practice.leaderboards.LeaderboardThread;
import me.lrxh.practice.leaderboards.command.LeaderboardsCommand;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.MatchListener;
import me.lrxh.practice.party.Party;
import me.lrxh.practice.party.PartyListener;
import me.lrxh.practice.profile.KillEffects;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileListener;
import me.lrxh.practice.profile.Themes;
import me.lrxh.practice.profile.hotbar.Hotbar;
import me.lrxh.practice.queue.QueueListener;
import me.lrxh.practice.queue.QueueThread;
import me.lrxh.practice.scoreboard.ScoreboardAdapter;
import me.lrxh.practice.setting.ProfileSettingsCommand;
import me.lrxh.practice.setting.SettingsCommand;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.Console;
import me.lrxh.practice.util.InventoryUtil;
import me.lrxh.practice.util.assemble.Assemble;
import me.lrxh.practice.util.config.BasicConfigurationFile;
import me.lrxh.practice.util.menu.MenuListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public class Practice extends JavaPlugin {

    private static Practice practice;
    private BasicConfigurationFile mainConfig;
    private BasicConfigurationFile arenasConfig;
    private BasicConfigurationFile kitsConfig;
    private BasicConfigurationFile scoreboardConfig;
    private BasicConfigurationFile menusConfig;
    private BasicConfigurationFile messagesConfig;
    private MongoDatabase mongoDatabase;
    private Cache cache;
    private PaperCommandManager paperCommandManager;
    private Assemble assemble;
    private boolean placeholder = false;
    private boolean replay = false;
    //private SpigotHandler spigotHandler;
    private Hotbar hotbar;

    public static Practice getInstance() {
        if (practice == null) {
            practice = new Practice();
        }
        return practice;
    }

    public void loadConfigs() {
        mainConfig = new BasicConfigurationFile(this, "config");
        arenasConfig = new BasicConfigurationFile(this, "arenas");
        kitsConfig = new BasicConfigurationFile(this, "kits");
        scoreboardConfig = new BasicConfigurationFile(this, "scoreboard");
        menusConfig = new BasicConfigurationFile(this, "menus");
        messagesConfig = new BasicConfigurationFile(this, "messages");
    }

    @Override
    public void onEnable() {
        long oldTime = System.currentTimeMillis();
        practice = this;
        loadConfigs();
        loadMongo();
//        spigotHandler = new SpigotHandler(practice);
//        if (Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].equals("v1_8_R3")) {
//            spigotHandler.init(false);
//        }

        cache = new Cache();
        hotbar = new Hotbar();
        Practice.getInstance().getHotbar().init();
        Kit.init();
        Arena.init();
        Profile.init();
        Match.init();
        Party.init();
        loadCommandManager();
        assemble = new Assemble(this, new ScoreboardAdapter());
        new QueueThread().start();
        new LeaderboardThread().start();
        new Metrics(this, 20915);

        Arrays.asList(
                new KitEditorListener(),
                new PartyListener(),
                new ProfileListener(),
                new PartyListener(),
                new MatchListener(),
                new QueueListener(),
                new ArenaListener(),
                new MenuListener()
        ).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));


        Arrays.asList(
                Material.WORKBENCH,
                Material.STICK,
                Material.WOOD_PLATE,
                Material.WOOD_BUTTON,
                Material.SNOW_BLOCK
        ).forEach(InventoryUtil::removeCrafting);

        // Set the difficulty for each world to HARD
        // Clear the droppedItems for each world
        getServer().getWorlds().forEach(world -> {
            world.setDifficulty(Difficulty.HARD);
        });

        for (World world : getInstance().getServer().getWorlds()) {
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doWeatherCycle", "false");
        }

        Plugin placeholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderAPI != null && placeholderAPI.isEnabled()) {
            new Placeholder().register();
            placeholder = true;
            Console.sendMessage(CC.translate("&aPlaceholderAPI found!"));
            Console.sendMessage(CC.translate("&aRegistering placeholders"));
        }
        Plugin advancedReplays = getServer().getPluginManager().getPlugin("AdvancedReplay");
        if (advancedReplays != null && advancedReplays.isEnabled()) {
            replay = true;
            Console.sendMessage(CC.translate("&aAdvancedReplay found!"));
        }
        Console.sendMessage(CC.translate("&7&m-----------------------------------------"));
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7| &cPractice Core"));
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7| &fAuthor(s): &c" + getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", "")));
        Console.sendMessage(CC.translate("&7| &fVersion: &c" + getInstance().getDescription().getVersion()));
        Console.sendMessage(CC.translate("&7| &fSpigot: &c" + getInstance().getServer().getName()));
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7| &fKits: &c" + Kit.getKits().size()));
        Console.sendMessage(CC.translate("&7| &fArenas: &c" + Arena.getArenas().size()));
//        if (spigotHandler != null) {
//            Console.sendMessage(CC.translate("&7| &fKB Controller: &c" + spigotHandler.getType()));
//        }
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7| &fPlugin Loaded in : &c" + (System.currentTimeMillis() - oldTime) + "ms"));
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7&m-----------------------------------------"));
        System.gc();
    }

    public void clearEntities() {
        for (World world : practice.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity.getType() == EntityType.PLAYER)) {
                    continue;
                }
                if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                    entity.remove();
                }
            }
        }
    }

    private void loadCommandManager() {
        paperCommandManager = new PaperCommandManager(getInstance());
        loadCommandCompletions();
        registerCommands();
        registerPermissions();
    }

    private void registerCommands() {

        Arrays.asList(
                new ArenaCommand(),
                new MainCommand(),
                new KitCommand(),
                new FlyCommand(),
                new SettingsCommand(),
                new DuelCommand(),
                new PingCommand(),
                new StatsCommand(),
                new ProfileSettingsCommand(),
                new LeaderboardsCommand(),
                new RematchCommand(),
                new ViewInventoryCommand(),
                new SpectateCommand(),
                new PartyCommand(),
                new EloCommand(),
                new SilentCommand(),
                new FollowCommand()
        ).forEach(command -> paperCommandManager.registerCommand(command));
    }

    private void loadCommandCompletions() {
        CommandCompletions<BukkitCommandCompletionContext> commandCompletions = getPaperCommandManager().getCommandCompletions();
        commandCompletions.registerCompletion("names", c -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        commandCompletions.registerCompletion("arenas", c -> Arena.getArenas().stream().map(Arena::getName).collect(Collectors.toList()));
        commandCompletions.registerCompletion("kits", c -> Kit.getKits().stream().map(Kit::getName).collect(Collectors.toList()));
    }

    private void registerPermissions() {
        PluginManager pluginManager = getServer().getPluginManager();
        for (KillEffects killEffect : KillEffects.values()) {
            pluginManager.addPermission(new Permission("practice.killeffect." + killEffect.getDisplayName(), PermissionDefault.OP));
        }
        for (Themes theme : Themes.values()) {
            pluginManager.addPermission(new Permission("practice.theme." + theme.getName(), PermissionDefault.OP));
        }
        Arrays.asList(
                "practice.admin.arena",
                "practice.donor.fly",
                "practice.admin.main",
                "practice.admin.kit"
        ).forEach(permission -> pluginManager.addPermission(new Permission(permission, PermissionDefault.OP)));
    }

    @Override
    public void onDisable() {
        clearEntities();
        Match.cleanup();
    }

    private void loadMongo() {
        String mongoUri = mainConfig.getString("MONGO.URI");

        //Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        //mongoLogger.setLevel(Level.WARNING);

        //for (Handler handler : mongoLogger.getParent().getHandlers()) {
        //    if (handler instanceof ConsoleHandler) {
        //        mongoLogger.getParent().removeHandler(handler);
        //    }
        //}
        if (mongoUri != null && !mongoUri.isEmpty()) {
            try {
                MongoClient mongoClient = MongoClients.create(new ConnectionString(mongoUri));
                mongoDatabase = mongoClient.getDatabase(mainConfig.getString("MONGO.DATABASE"));
            } catch (Exception e) {
                getLogger().warning("Error connecting to MongoDB:" + e.getMessage());
            }
        } else {
            Console.sendError("MongoDB URI is missing or empty in the config.yml");
        }
    }
}
