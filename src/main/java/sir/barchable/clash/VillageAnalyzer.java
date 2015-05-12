package sir.barchable.clash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.VillageStats.Defense;
import sir.barchable.clash.model.*;
import sir.barchable.clash.model.LootCalculator.Loot;
import sir.barchable.clash.model.LootCalculator.LootCollection;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.model.json.WarVillage;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.proxy.MessageTap;
import sir.barchable.clash.proxy.ProxySession;
import sir.barchable.util.Dates;
import sir.barchable.util.Json;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;
import static sir.barchable.clash.protocol.Pdu.Type.OwnHomeData;

/**
 * @author Sir Barchable
 *         Date: 20/04/15
 */
public class VillageAnalyzer implements MessageTap {
    private static final Logger log = LoggerFactory.getLogger(VillageAnalyzer.class);
    public static final String CLAN_STATS_PREFIX = "clan.stats.";

    private Logic logic;
    private LootCalculator lootCalculator;

    public VillageAnalyzer(Logic logic) {
        this.logic = logic;
        this.lootCalculator = new LootCalculator(logic);
    }

    @Override
    public void onMessage(Message message) {
        String homeVillage = (String) message.get("homeVillage");

        switch (message.getType()) {
            case OwnHomeData:
            case VisitedHomeData:
            case EnemyHomeData:
                try {
                    Village village = Json.valueOf(homeVillage, Village.class);
                    analyzeHomeVillage(message, village);
                } catch (RuntimeException | IOException e) {
                    log.warn("Could not read village", e);
                }
                break;

            case WarHomeData:
                try {
                    WarVillage village = Json.valueOf(homeVillage, WarVillage.class);
                    analyzeWarVillage(message, village);
                } catch (IOException e) {
                    log.warn("Could not read village", e);
                }
                break;
        }
    }

    private void analyzeHomeVillage(Message message, Village village) {
        SessionState sessionState = ProxySession.getSession().getSessionState();

        int age = message.getInt("age");
        Integer timeStamp = message.getInt("timeStamp");

        Message user = message.getMessage("user");
        String userName = user.getString("userName");
        Long userId = user.getLong("userId");

        Map<String, Object> clan = (Map<String, Object>) user.get("clan");
        String clanName = null;
        if (clan != null) {
            clanName = (String) clan.get("clanName");
        }

        int townHallLevel = 0;
        int dpsTotal = 0;
        int hpTotal = 0;
        int wallHpTotal = 0;

        Map<String, Integer> collectorTotals = new LinkedHashMap<>();
        collectorTotals.put("Elixir", 0);
        collectorTotals.put("Gold", 0);
        collectorTotals.put("DarkElixir", 0);

        //
        // Sum stats for all buildings
        //

        for (Village.Building building : village.buildings) {
            int typeId = building.data;
            String buildingName;
            try {
                buildingName = logic.getSubTypeName(typeId);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown building of type {}. Perhaps you need a logic update.", typeId);
                continue;
            }

            int level = building.lvl == null || building.lvl == -1 ? 0 : building.lvl;

            if (typeId == ObjectType.TOWN_HALL) {
                townHallLevel = level + 1;
            }

            //
            // Hit points and damage
            //

            int dps = logic.getInt(typeId, "Damage", level);

            if (!"Tesla Tower".equals(buildingName)) { // Count teslas as traps
                dpsTotal += dps;

                int hp = logic.getInt(typeId, "Hitpoints", level);
                if (typeId == ObjectType.WALL) {
                    wallHpTotal += hp;
                } else {
                    hpTotal += hp;
                }
            }

            //
            // Collectors
            //

            if (building.res_time != null && building.const_t == null) {
                // res_time needs to be adjusted by the age of the data
                int resTime = building.res_time - age;
                // fetch generation parameters for the collector
                String resourceName = logic.getString(typeId, "ProducesResource");
                int resourcePerHour = logic.getInt(typeId, "ResourcePerHour", level);
                int resourceMax = logic.getInt(typeId, "ResourceMax", level);

                // Total time to fill
                int maxTime = 3600 * resourceMax / resourcePerHour;

                int resourceValue;
                if (resTime >= maxTime) {
                    resourceValue = 0;
                } else if (resTime <= 0) {
                    resourceValue = resourceMax;
                } else {
                    // Time passed since reset
                    int timePassed = maxTime - resTime;
                    // Resources produced during that time

//                    resourceValue = timePassed * resourcePerHour / 3600;

                    int h = timePassed / 3600;
                    int m = timePassed % 3600 / 60;
                    int s = timePassed % 60;
                    resourceValue = h * resourcePerHour + m * resourcePerHour / 60 + s * resourcePerHour / 3600;
                }

                if (building.boost_t != null) {
                    resourceValue *= logic.getInt("globals:RESOURCE_PRODUCTION_BOOST_MULTIPLIER", "NumberValue");
                }

                // Accumulate total
                collectorTotals.put(resourceName, collectorTotals.get(resourceName) + resourceValue);
            }
        }

        VillageStats villageStats = new VillageStats(userName, new Defense(hpTotal, wallHpTotal, dpsTotal));

        //
        // Storage
        //

        Message resources = message.getMessage("resources");
        LootCollection loot = sumStorage(resources).withCollectorLoot(
            new Loot(collectorTotals.get("Gold"), collectorTotals.get("Elixir"), collectorTotals.get("DarkElixir"))
        );

        int timeToGemboxDrop = village.respawnVars.time_to_gembox_drop < 0 ? 0 : village.respawnVars.time_to_gembox_drop;
        if (message.getType() == OwnHomeData) {
            if (sessionState.getUserId() == 0) {
                log.info("Welcome {}", userName);
                // OwnHomeData. Remember town hall level for loot calculations
                sessionState.setUserId(userId);
                // Log startup info
                log.info("Clock skew is {}ms", System.currentTimeMillis() - timeStamp * 1000l);
                log.info("Gem box time in period {}", Dates.formatInterval(village.respawnVars.time_in_gembox_period));
            }
            sessionState.setUserName(userName);
            sessionState.setTownHallLevel(townHallLevel);
        } else {
            loot = lootCalculator.calculateAvailableLoot(loot, townHallLevel);
        }

        //
        // Garrison
        //

        List<String> unitDescriptions = new ArrayList<>();
        Object[] castleUnits = (Object[]) resources.get("allianceUnits");
        for (int i = 0; i < castleUnits.length; i++) {
            Map<String, Object> castleUnit = (Map<String, Object>) castleUnits[i];
            int count = (Integer) castleUnit.get("count");
            if (count > 0) {
                int level = (Integer) castleUnit.get("level") + 1;
                int typeId = (Integer) castleUnit.get("typeId");
                String unitName = logic.getSubTypeName(typeId);
                unitDescriptions.add("lvl " + level + " " + unitName + " x " + count);
            }
        }

        //
        // Dump stats
        //

        log.info("{}", userName);
        log.info("Gem box drop {}", Dates.formatInterval(timeToGemboxDrop));
        log.info("DPS: {}, HP: {} (walls {})", dpsTotal, hpTotal, wallHpTotal);
        log.info("Garrison: " + unitDescriptions);
        log.info("Loot:");
        log.info("Town Hall: {}", loot.getTownHallLoot());
        log.info("Storage: {}", loot.getStorageLoot());
        log.info("Castle: {}", loot.getCastleLoot());
        log.info("Collectors: {}", loot.getCollectorLoot());
        log.info("Total: {}", loot.total());

        if (message.getType() != OwnHomeData) {
            // Apply raid penalty
            if (sessionState.getTownHallLevel() == 0) {
                log.warn("User town hall level not set, can't calculate loot penalty.");
            } else {
                int penalty = lootCalculator.getLevelPenalty(sessionState.getTownHallLevel(), townHallLevel);
                if (penalty != 100) {
                    log.info("After penalty of {}%: {}", 100 - penalty, loot.total().percent(penalty));
                }
            }
        }

        // Save the stats in the session.
        if (clanName != null) {
            // We keep the collection of stats for each clan in a a map from village ID -> stats
            String statKey = CLAN_STATS_PREFIX + clanName;
            Map<Long, VillageStats> clanStats = (Map<Long, VillageStats>) sessionState.getAttribute(statKey);
            if (clanStats == null) {
                clanStats = new HashMap<>();
                sessionState.setAttribute(statKey, clanStats);
            }
            clanStats.put(userId, villageStats);
        }
    }

    public LootCollection sumStorage(Message resources) {
        Map<String, Integer> storageTotals = new LinkedHashMap<>();
        storageTotals.put("Elixir", 0);
        storageTotals.put("Gold", 0);
        storageTotals.put("DarkElixir", 0);
        storageTotals.put("WarElixir", 0);
        storageTotals.put("WarGold", 0);
        storageTotals.put("WarDarkElixir", 0);

        if (resources != null) {
            Message[] resourceCounts = resources.getArray("resourceCounts");
            for (Message resourceCount : resourceCounts) {
                int typeId = resourceCount.getInt("type");
                String storageType = logic.getSubTypeName(typeId);
                int resourceValue = resourceCount.getInt("value");
                storageTotals.put(storageType, resourceValue);
            }
        }

        int thGold = min(storageTotals.get("Gold"), 1000);
        int thElixir = min(storageTotals.get("Elixir"), 1000);

        return new LootCollection(
            Loot.ZERO,
            new Loot(storageTotals.get("Gold") - thGold, storageTotals.get("Elixir") - thElixir, storageTotals.get("DarkElixir")),
            new Loot(storageTotals.get("WarGold"), storageTotals.get("WarElixir"), storageTotals.get("WarDarkElixir")),
            new Loot(thGold, thElixir, 0)
        );
    }

    private void analyzeWarVillage(Message message, WarVillage village) {
        SessionState sessionState = ProxySession.getSession().getSessionState();

        long userId = (long) village.avatar_id_high << 32 | village.avatar_id_low & 0xffffffffl;
        String userName = village.name;
        String clanName = village.alliance_name;

        int dpsTotal = 0;
        int hpTotal = 0;
        int wallHpTotal = 0;

        //
        // Sum stats for all buildings
        //

        for (WarVillage.Building building : village.buildings) {
            int typeId = building.data;
            String buildingName;
            try {
                buildingName = logic.getSubTypeName(typeId);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown building of type {}. Perhaps you need a logic update.", typeId);
                continue;
            }
            int level = building.lvl == null || building.lvl == -1 ? 0 : building.lvl;

            //
            // Hit points and damage
            //

            int dps = logic.getInt(typeId, "Damage", level);
            dpsTotal += dps;

            int hp = logic.getInt(typeId, "Hitpoints", level);
            if (typeId == ObjectType.WALL) {
                wallHpTotal += hp;
            } else {
                hpTotal += hp;
            }
        }

        VillageStats villageStats = new VillageStats(userName, new Defense(hpTotal, wallHpTotal, dpsTotal));

        //
        // Garrison
        //

        List<String> unitDescriptions = new ArrayList<>();
        Unit[] castleUnits = village.alliance_units;
        if (castleUnits != null) {
            for (Unit unit : castleUnits) {
                int count = unit.getCnt();
                if (count > 0) {
                    int level = unit.getLvl() + 1;
                    int typeId = unit.getId();
                    String unitName = logic.getSubTypeName(typeId);
                    unitDescriptions.add("lvl " + level + " " + unitName + " x " + count);
                }
            }
        }

        //
        // Dump stats
        //

        log.info("{}", userName);
        log.info("DPS: {}, HP: {} (walls {})", dpsTotal, hpTotal, wallHpTotal);
        log.info("Garrison: " + unitDescriptions);

        // Save the stats in the session.
        if (clanName != null) {
            // We keep the collection of stats for each clan in a a map from village ID -> stats
            String statKey = CLAN_STATS_PREFIX + clanName;
            Map<Long, VillageStats> clanStats = (Map<Long, VillageStats>) sessionState.getAttribute(statKey);
            if (clanStats == null) {
                clanStats = new HashMap<>();
                sessionState.setAttribute(statKey, clanStats);
            }
            clanStats.put(userId, villageStats);
        }
    }

    /**
     * Summarise collected stats on session end.
     */
    public static void logSession(ProxySession session) {
        // For each session attribute with a key that starts "clan.stats."
        session
            .getSessionState()
            .getAttributes()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().startsWith(CLAN_STATS_PREFIX))
            .forEach(entry -> {
                // Sum the stats for each village in the clan
                Map<String, VillageStats> villageStats = (Map<String, VillageStats>) entry.getValue();
                if (villageStats.size() > 1) {
                    Optional<Defense> defensiveTotal = villageStats
                        .values()
                        .stream()
                        .map(VillageStats::getDefense)
                        .reduce(Defense::add);

                    // and print them
                    String clanName = entry.getKey().substring(CLAN_STATS_PREFIX.length());
                    log.info(villageStats.size() + " villages in " + clanName + " with total " + defensiveTotal.get());
                }
            });
    }
}
