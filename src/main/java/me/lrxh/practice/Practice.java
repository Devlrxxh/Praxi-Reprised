package me.lrxh.practice;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import me.lrxh.practice.arena.*;
import me.lrxh.practice.arena.command.ArenaCommand;
import me.lrxh.practice.commands.admin.general.MainCommand;
import me.lrxh.practice.commands.donater.FlyCommand;
import me.lrxh.practice.commands.event.map.*;
import me.lrxh.practice.commands.event.user.HostCommand;
import me.lrxh.practice.commands.event.vote.EventMapVoteCommand;
import me.lrxh.practice.commands.user.PingCommand;
import me.lrxh.practice.commands.user.StatsCommand;
import me.lrxh.practice.commands.user.duels.DuelAcceptCommand;
import me.lrxh.practice.commands.user.duels.DuelCommand;
import me.lrxh.practice.commands.user.duels.RematchCommand;
import me.lrxh.practice.commands.user.match.SpectateCommand;
import me.lrxh.practice.commands.user.match.StopSpectatingCommand;
import me.lrxh.practice.commands.user.match.ViewInventoryCommand;
import me.lrxh.practice.commands.user.party.*;
import me.lrxh.practice.essentials.Essentials;
import me.lrxh.practice.event.Event;
import me.lrxh.practice.event.EventTypeAdapter;
import me.lrxh.practice.event.game.EventGameListener;
import me.lrxh.practice.event.game.map.EventGameMap;
import me.lrxh.practice.event.game.map.EventGameMapTypeAdapter;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.kit.KitEditorListener;
import me.lrxh.practice.kit.KitTypeAdapter;
import me.lrxh.practice.kit.command.KitCommand;
import me.lrxh.practice.leaderboards.LeaderboardThread;
import me.lrxh.practice.leaderboards.LeaderboardsCommand;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.match.MatchListener;
import me.lrxh.practice.party.Party;
import me.lrxh.practice.party.PartyListener;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.profile.ProfileListener;
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
import me.lrxh.practice.util.command.Honcho;
import me.lrxh.practice.util.config.BasicConfigurationFile;
import me.lrxh.practice.util.menu.MenuListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.refinedev.api.spigot.SpigotHandler;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public class Practice extends JavaPlugin {

    private static Practice practice;

    private BasicConfigurationFile mainConfig;
    private BasicConfigurationFile arenasConfig;
    private BasicConfigurationFile kitsConfig;
    private BasicConfigurationFile eventsConfig;
    private BasicConfigurationFile scoreboardConfig;
    private BasicConfigurationFile menusConfig;
    private MongoDatabase mongoDatabase;
    private Honcho honcho;
    private Essentials essentials;
    private Cache cache;
    private PaperCommandManager paperCommandManager;
    private Assemble assemble;
    private SpigotHandler spigotHandler;

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
        eventsConfig = new BasicConfigurationFile(this, "events");
        scoreboardConfig = new BasicConfigurationFile(this, "scoreboard");
        menusConfig = new BasicConfigurationFile(this, "menus");
        this.essentials = new Essentials(this);
    }

    @Override
    public void onEnable() {
        long oldTime = System.currentTimeMillis();
        practice = this;
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        honcho = new Honcho(this);
        loadConfigs();
        loadMongo();
        spigotHandler = new SpigotHandler(practice);
        if(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].equals("v1_8_R3")){
            spigotHandler.init(false);
        }

        cache = new Cache();
        Hotbar.init();
        Kit.init();
        Arena.init();
        Profile.init();
        Match.init();
        Party.init();
        Event.init();
        EventGameMap.init();
        loadCommandManager();
        assemble = new Assemble(this, new ScoreboardAdapter());
        new QueueThread().start();
        new LeaderboardThread().start();
        new Metrics(this, 20915);

        getHoncho().registerTypeAdapter(Arena.class, new ArenaTypeAdapter());
        getHoncho().registerTypeAdapter(ArenaType.class, new ArenaTypeTypeAdapter());
        getHoncho().registerTypeAdapter(Kit.class, new KitTypeAdapter());
        getHoncho().registerTypeAdapter(EventGameMap.class, new EventGameMapTypeAdapter());
        getHoncho().registerTypeAdapter(Event.class, new EventTypeAdapter());

        Arrays.asList(
                new DuelCommand(),
                new DuelAcceptCommand(),
                new EventMapCreateCommand(),
                new EventMapDeleteCommand(),
                new EventMapsCommand(),
                new EventMapSetSpawnCommand(),
                new EventMapStatusCommand(),
                new EventMapVoteCommand(),
                new RematchCommand(),
                new SpectateCommand(),
                new StopSpectatingCommand(),
                new PartyChatCommand(),
                new PartyCloseCommand(),
                new PartyCreateCommand(),
                new PartyDisbandCommand(),
                new PartyHelpCommand(),
                new PartyInfoCommand(),
                new PartyInviteCommand(),
                new PartyJoinCommand(),
                new PartyKickCommand(),
                new PartyLeaveCommand(),
                new PartyOpenCommand(),
                new ViewInventoryCommand()
        ).forEach(command -> getHoncho().registerCommand(command));
        Arrays.asList(
                new KitEditorListener(),
                new PartyListener(),
                new ProfileListener(),
                new PartyListener(),
                new MatchListener(),
                new QueueListener(),
                new ArenaListener(),
                new EventGameListener()
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
            getEssentials().clearEntities(world);
        });
        Plugin placeholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderAPI != null && placeholderAPI.isEnabled()) {
            new Placeholder().register();
            Console.sendMessage(CC.translate("&aPlaceholderAPI found!"));
            Console.sendMessage(CC.translate("&aRegistering placeholders"));
        }
        Console.sendMessage(CC.translate("&7&m-----------------------------------------"));
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7| &cPractice Core"));
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7| &fAutor(s): &c" + getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", "")));
        Console.sendMessage(CC.translate("&7| &fVersion: &c" + getInstance().getDescription().getVersion()));
        Console.sendMessage(CC.translate("&7| &fSpigot: &c" + getInstance().getServer().getName()));
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7| &fKits: &c" + Kit.getKits().size()));
        Console.sendMessage(CC.translate("&7| &fArenas: &c" + Arena.getArenas().size()));
        if (spigotHandler != null) {
            Console.sendMessage(CC.translate("&7| &fKB Controller: &c" + spigotHandler.getType()));
        }
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7| &fPlugin Loaded in : &c" + (System.currentTimeMillis() - oldTime) + "ms"));
        Console.sendMessage(CC.translate(" "));
        Console.sendMessage(CC.translate("&7&m-----------------------------------------"));
        System.gc();
    }

    private void loadCommandManager() {
        paperCommandManager = new PaperCommandManager(getInstance());
        loadCommandCompletions();
        registerCommands();
    }

    private void registerCommands() {
        Arrays.asList(
                new ArenaCommand(),
                new MainCommand(),
                new KitCommand(),
                new FlyCommand(),
                new SettingsCommand(),
                new HostCommand(),
                new PingCommand(),
                new StatsCommand(),
                new ProfileSettingsCommand(),
                new LeaderboardsCommand()
        ).forEach(command -> paperCommandManager.registerCommand(command));
    }

    private void loadCommandCompletions() {
        CommandCompletions<BukkitCommandCompletionContext> commandCompletions = getPaperCommandManager().getCommandCompletions();
        commandCompletions.registerCompletion("names", c -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        commandCompletions.registerCompletion("arenas", c -> Arena.getArenas().stream().map(Arena::getName).collect(Collectors.toList()));
        commandCompletions.registerCompletion("kits", c -> Kit.getKits().stream().map(Kit::getName).collect(Collectors.toList()));
        commandCompletions.registerCompletion("events", c -> Event.events.stream().map(Event::getDisplayName).collect(Collectors.toList()));
        commandCompletions.registerCompletion("maps", c -> EventGameMap.getMaps().stream().map(EventGameMap::getMapName).collect(Collectors.toList()));
    }

    @Override
    public void onDisable() {
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
