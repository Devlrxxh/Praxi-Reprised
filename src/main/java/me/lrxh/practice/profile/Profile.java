package me.lrxh.practice.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.Practice;
import me.lrxh.practice.duel.DuelProcedure;
import me.lrxh.practice.duel.DuelRequest;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.kit.KitLoadout;
import me.lrxh.practice.match.Match;
import me.lrxh.practice.party.Party;
import me.lrxh.practice.profile.meta.ProfileKitData;
import me.lrxh.practice.profile.meta.ProfileKitEditorData;
import me.lrxh.practice.profile.meta.ProfileRematchData;
import me.lrxh.practice.profile.meta.option.ProfileOptions;
import me.lrxh.practice.queue.QueueProfile;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.Cooldown;
import me.lrxh.practice.util.InventoryUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
@Setter
public class Profile {

    @Getter
    private static final Map<UUID, Profile> profiles = new HashMap<>();
    public static MongoCollection<Document> collection;
    private final ProfileOptions options;
    private final ProfileKitEditorData kitEditorData;
    private final Map<Kit, ProfileKitData> kitData;
    private final List<DuelRequest> duelRequests;
    private final UUID uuid;
    private final String username;
    private final List<UUID> followers;
    private final List<UUID> following;
    private ProfileState state;
    private DuelProcedure duelProcedure;
    private ProfileRematchData rematchData;
    private Party party;
    private Match match;
    private QueueProfile queueProfile;
    private Cooldown enderpearlCooldown;
    private Cooldown voteCooldown;
    private boolean silent = false;

    public Profile(UUID uuid) {
        this.uuid = uuid;
        this.username = Bukkit.getPlayer(uuid).getName();
        this.state = ProfileState.LOBBY;
        this.options = new ProfileOptions();
        this.kitEditorData = new ProfileKitEditorData();
        this.kitData = new HashMap<>();
        this.duelRequests = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.enderpearlCooldown = new Cooldown(0);
        this.voteCooldown = new Cooldown(0);

        for (Kit kit : Kit.getKits()) {
            this.kitData.put(kit, new ProfileKitData());
        }
    }

    public static void init() {
        collection = Practice.getInstance().getMongoDatabase().getCollection("profiles");

        // Players might have joined before the plugin finished loading
        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = new Profile(player.getUniqueId());

            try {
                profile.load();
            } catch (Exception e) {
                player.kickPlayer(CC.RED + "The server is loading...");
                continue;
            }

            profiles.put(player.getUniqueId(), profile);
        }

        // Expire duel requests
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Profile profile : Profile.getProfiles().values()) {
                    Iterator<DuelRequest> iterator = profile.duelRequests.iterator();

                    while (iterator.hasNext()) {
                        DuelRequest duelRequest = iterator.next();

                        if (duelRequest.isExpired()) {
                            duelRequest.expire();
                            iterator.remove();
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(Practice.getInstance(), 60L, 60L);

    }

    public static Profile getByUuid(UUID uuid) {
        Profile profile = profiles.get(uuid);

        if (profile == null) {
            profile = new Profile(uuid);
        }

        return profile;
    }

    public boolean isEnderpearlOnCooldown() {
        return enderpearlCooldown != null && !enderpearlCooldown.hasExpired();
    }

    public int getWins() {
        int wins = 0;
        for (Map.Entry<Kit, ProfileKitData> entry : this.getKitData().entrySet()) {
            ProfileKitData profileKitData = entry.getValue();
            wins += profileKitData.getWon();
        }
        return wins;
    }

    public int getLoses() {
        int loses = 0;
        for (Map.Entry<Kit, ProfileKitData> entry : this.getKitData().entrySet()) {
            ProfileKitData profileKitData = entry.getValue();
            loses += profileKitData.getLost();
        }
        return loses;
    }

    public int getElo() {
        int elo = 0;
        int totalQueue = 0;

        for (Map.Entry<Kit, ProfileKitData> entry : this.getKitData().entrySet()) {
            ProfileKitData profileKitData = entry.getValue();
            elo += profileKitData.getElo();
            totalQueue++;
        }
        if (totalQueue == 0) {
            return 0;
        }
        return elo / totalQueue;
    }


    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public DuelRequest getDuelRequest(Player sender) {
        for (DuelRequest duelRequest : duelRequests) {
            if (duelRequest.getSender().equals(sender.getUniqueId())) {
                return duelRequest;
            }
        }

        return null;
    }


    public boolean isDuelRequestExpired(DuelRequest duelRequest) {
        if (duelRequest != null) {
            if (duelRequest.isExpired()) {
                duelRequests.remove(duelRequest);
                return true;
            }
        }

        return false;
    }

    public boolean isBusy() {
        return state != ProfileState.LOBBY;
    }

    public void load() {
        Document document = collection.find(Filters.eq("uuid", uuid.toString())).first();

        if (document == null) {
            this.save();
            return;
        }

        Document options = (Document) document.get("options");

        this.options.showScoreboard(options.getBoolean("showScoreboard"));
        this.options.allowSpectators(options.getBoolean("allowSpectators"));
        this.options.receiveDuelRequests(options.getBoolean("receiveDuelRequests"));
        this.options.killEffect(KillEffects.valueOf(options.getString("killeffect")));
        this.options.showPlayers(options.getBoolean("showPlayers"));
        this.options.theme(Themes.valueOf(options.getString("theme")));
        this.options.pingRange(options.getInteger("pingRange"));
        this.options.menuSounds(options.getBoolean("menuSounds"));
        this.options.time(Times.valueOf(options.getString("time")));

        Document kitStatistics = (Document) document.get("kitStatistics");

        for (String key : kitStatistics.keySet()) {
            Document kitDocument = (Document) kitStatistics.get(key);
            Kit kit = Kit.getByName(key);

            if (kit != null) {
                ProfileKitData profileKitData = new ProfileKitData();
                profileKitData.setElo(kitDocument.getInteger("elo"));
                profileKitData.setWon(kitDocument.getInteger("won"));
                profileKitData.setLost(kitDocument.getInteger("lost"));

                kitData.put(kit, profileKitData);
            }
        }

        Document kitsDocument = (Document) document.get("loadouts");

        for (String key : kitsDocument.keySet()) {
            Kit kit = Kit.getByName(key);

            if (kit != null) {
                JsonArray kitsArray = new JsonParser().parse(kitsDocument.getString(key)).getAsJsonArray();
                KitLoadout[] loadouts = new KitLoadout[4];

                for (JsonElement kitElement : kitsArray) {
                    JsonObject kitObject = kitElement.getAsJsonObject();

                    KitLoadout loadout = new KitLoadout(kitObject.get("name").getAsString());
                    loadout.setArmor(InventoryUtil.deserializeInventory(kitObject.get("armor").getAsString()));
                    loadout.setContents(InventoryUtil.deserializeInventory(kitObject.get("contents").getAsString()));

                    loadouts[kitObject.get("index").getAsInt()] = loadout;
                }

                kitData.get(kit).setLoadouts(loadouts);
            }
        }
    }

    public void save() {

        Document document = new Document();
        document.put("uuid", uuid.toString());
        document.put("username", username);

        Document optionsDocument = new Document();
        optionsDocument.put("showScoreboard", options.showScoreboard());
        optionsDocument.put("allowSpectators", options.allowSpectators());
        optionsDocument.put("receiveDuelRequests", options.receiveDuelRequests());
        optionsDocument.put("killeffect", options.killEffect().toString());
        optionsDocument.put("showPlayers", options.showPlayers());
        optionsDocument.put("theme", options.theme().toString());
        optionsDocument.put("pingRange", options.pingRange());
        optionsDocument.put("menuSounds", options.menuSounds());
        optionsDocument.put("time", options.time().toString());

        document.put("options", optionsDocument);

        Document kitStatisticsDocument = new Document();

        for (Map.Entry<Kit, ProfileKitData> entry : kitData.entrySet()) {
            Document kitDocument = new Document();
            kitDocument.put("elo", entry.getValue().getElo());
            kitDocument.put("won", entry.getValue().getWon());
            kitDocument.put("lost", entry.getValue().getLost());
            kitStatisticsDocument.put(entry.getKey().getName(), kitDocument);
        }

        document.put("kitStatistics", kitStatisticsDocument);

        Document kitsDocument = new Document();

        for (Map.Entry<Kit, ProfileKitData> entry : kitData.entrySet()) {
            JsonArray kitsArray = new JsonArray();

            for (int i = 0; i < 4; i++) {
                KitLoadout loadout = entry.getValue().getLoadout(i);

                if (loadout != null) {
                    JsonObject kitObject = new JsonObject();
                    kitObject.addProperty("index", i);
                    kitObject.addProperty("name", loadout.getCustomName());
                    kitObject.addProperty("armor", InventoryUtil.serializeInventory(loadout.getArmor()));
                    kitObject.addProperty("contents", InventoryUtil.serializeInventory(loadout.getContents()));
                    kitsArray.add(kitObject);
                }
            }

            kitsDocument.put(entry.getKey().getName(), kitsArray.toString());
        }

        document.put("loadouts", kitsDocument);

        collection.replaceOne(Filters.eq("uuid", uuid.toString()), document, new ReplaceOptions().upsert(true));
    }

}
