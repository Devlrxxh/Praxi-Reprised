package me.funky.praxi;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import me.funky.praxi.adapter.CoreManager;
import me.funky.praxi.arena.*;
import me.funky.praxi.arena.command.ArenaCommand;
import me.funky.praxi.commands.admin.general.MainCommand;
import me.funky.praxi.commands.donater.FlyCommand;
import me.funky.praxi.commands.event.map.*;
import me.funky.praxi.commands.event.user.HostCommand;
import me.funky.praxi.commands.event.vote.EventMapVoteCommand;
import me.funky.praxi.commands.user.duels.DuelAcceptCommand;
import me.funky.praxi.commands.user.duels.DuelCommand;
import me.funky.praxi.commands.user.duels.RematchCommand;
import me.funky.praxi.commands.user.gamer.SuicideCommand;
import me.funky.praxi.commands.user.match.SpectateCommand;
import me.funky.praxi.commands.user.match.StopSpectatingCommand;
import me.funky.praxi.commands.user.match.ViewInventoryCommand;
import me.funky.praxi.commands.user.party.*;
import me.funky.praxi.commands.user.settings.ToggleDuelRequestsCommand;
import me.funky.praxi.commands.user.settings.ToggleScoreboardCommand;
import me.funky.praxi.commands.user.settings.ToggleSpectatorsCommand;
import me.funky.praxi.essentials.Essentials;
import me.funky.praxi.event.Event;
import me.funky.praxi.event.EventTypeAdapter;
import me.funky.praxi.event.game.EventGameListener;
import me.funky.praxi.event.game.map.EventGameMap;
import me.funky.praxi.event.game.map.EventGameMapTypeAdapter;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.kit.KitEditorListener;
import me.funky.praxi.kit.KitTypeAdapter;
import me.funky.praxi.kit.command.KitCommand;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.MatchListener;
import me.funky.praxi.party.Party;
import me.funky.praxi.party.PartyListener;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.ProfileListener;
import me.funky.praxi.profile.hotbar.Hotbar;
import me.funky.praxi.queue.QueueListener;
import me.funky.praxi.queue.QueueThread;
import me.funky.praxi.scoreboard.ScoreboardAdapter;
import me.funky.praxi.setting.SettingsCommand;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.InventoryUtil;
import me.funky.praxi.util.assemble.Assemble;
import me.funky.praxi.util.command.Honcho;
import me.funky.praxi.util.config.BasicConfigurationFile;
import me.funky.praxi.util.menu.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.refinedev.api.spigot.SpigotHandler;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public class Praxi extends JavaPlugin {

    private static Praxi praxi;

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

    public static Praxi getInstance() {
        if (praxi == null) {
            praxi = new Praxi();
        }
        return praxi;
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
        praxi = this;
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        honcho = new Honcho(this);
        loadConfigs();
        loadMongo();
        spigotHandler = new SpigotHandler(praxi);
        spigotHandler.init(true);
        if (spigotHandler != null) {
            Bukkit.getServer().getConsoleSender().sendMessage(CC.translate("&cHooked into: &7" + spigotHandler.getType()));
        }
        new CoreManager();
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
                new ViewInventoryCommand(),
                new ToggleScoreboardCommand(),
                new ToggleSpectatorsCommand(),
                new ToggleDuelRequestsCommand(),
                new SuicideCommand()
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
                new HostCommand()
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

        if (mongoUri != null && !mongoUri.isEmpty()) {
            mongoDatabase = MongoClients.create(new ConnectionString(mongoUri))
                    .getDatabase(mainConfig.getString("MONGO.DATABASE"));
        } else {
            getLogger().warning("MongoDB URI is not in the config.yml.");
        }
    }

}
