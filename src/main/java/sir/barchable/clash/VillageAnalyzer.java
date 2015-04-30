package sir.barchable.clash;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.VillageStats.Defense;
import sir.barchable.clash.model.*;
import sir.barchable.clash.model.LootCalculator.Loot;
import sir.barchable.clash.model.LootCalculator.LootCollection;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.model.json.WarVillage;
import sir.barchable.clash.proxy.MessageTap;
import sir.barchable.clash.proxy.ProxySession;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;
import static sir.barchable.clash.protocol.Pdu.ID.OwnHomeData;

/**
 * @author Sir Barchable
 *         Date: 20/04/15
 */
public class VillageAnalyzer implements MessageTap {
    private static final Logger log = LoggerFactory.getLogger(VillageAnalyzer.class);
    public static final String CLAN_STATS_PREFIX = "clan.stats.";

    private ObjectMapper mapper = new ObjectMapper();
    private Logic logic;
    private LootCalculator lootCalculator;

    public VillageAnalyzer(Logic logic) {
        this.logic = logic;
        this.lootCalculator = new LootCalculator(logic);
    }

    @Override
    public void onMessage(int id, Map<String, Object> message) {
        ProxySession.SessionData sessionData = ProxySession.getSession().getSessionData();

        String warVillage = (String) message.get("warVillage");
        if (warVillage != null) {
            try {
                WarVillage village = mapper.readValue(warVillage, WarVillage.class);
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String homeVillage = (String) message.get("homeVillage");
        if (homeVillage != null) {
            try {
                Village village = mapper.readValue(homeVillage, Village.class);

                Integer timeStamp = (Integer) message.get("timeStamp"); // only present in own home data
                int age = (Integer) message.get("age");

                Map<String, Object> user = (Map<String, Object>) message.get("user");
                String userName = (String) user.get("userName");
                Long userId = (Long) user.get("userId");

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

                Map<String, Integer> storageTotals = new LinkedHashMap<>();
                storageTotals.put("Elixir", 0);
                storageTotals.put("Gold", 0);
                storageTotals.put("DarkElixir", 0);
                storageTotals.put("WarElixir", 0);
                storageTotals.put("WarGold", 0);
                storageTotals.put("WarDarkElixir", 0);

                //
                // Sum stats for all buildings
                //

                for (Village.Building building : village.buildings) {
                    int typeId = building.data;
                    int level = building.lvl == null || building.lvl == -1 ? 0 : building.lvl;

                    if (typeId == ObjectType.TOWN_HALL) {
                        townHallLevel = level + 1;
                    }

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
                            resourceValue = timePassed * resourcePerHour / 3600;
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

                Map<String, Object> resources = (Map<String, Object>) message.get("resources");
                if (resources != null) {
                    Object[] resourceCounts = (Object[]) resources.get("resourceCounts");
                    for (int i = 0; i < resourceCounts.length; i++) {
                        Map<String, Object> resource = (Map<String, Object>) resourceCounts[i];
                        int typeId = (int) resource.get("type");
                        String storageType = logic.getSubTypeName(typeId);
                        int resourceValue = (int) resource.get("value");
                        storageTotals.put(storageType, resourceValue);
                    }
                }

                // todo: Does the town hall get the first 1000, or is it shared with other storage if below 1000?
                int thGold = min(storageTotals.get("Gold"), 1000);
                int thElixir = min(storageTotals.get("Elixir"), 1000);

                LootCollection loot = new LootCollection(
                    new Loot(collectorTotals.get("Gold"), collectorTotals.get("Elixir"), collectorTotals.get("DarkElixir")),
                    new Loot(storageTotals.get("Gold") - thGold, storageTotals.get("Elixir") - thElixir, storageTotals.get("DarkElixir")),
                    new Loot(storageTotals.get("WarGold"), storageTotals.get("WarElixir"), storageTotals.get("WarDarkElixir")),
                    new Loot(thGold, thElixir, 0)
                );

                if (id == OwnHomeData.id()) {
                    // OwnHomeData. Remember town hall level for loot calculations
                    sessionData.setUserName(userName);
                    sessionData.setUserId(userId);
                    sessionData.setTownHallLevel(townHallLevel);
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
                log.info("DPS: {}, HP: {} (walls {})", dpsTotal, hpTotal, wallHpTotal);
                log.info("Garrison: " + unitDescriptions);
                log.info("Town Hall: {}", loot.getTownHallLoot());
                log.info("Storage: {}", loot.getStorageLoot());
                log.info("Castle: {}", loot.getCastleLoot());
                log.info("Collectors: {}", loot.getCollectorLoot());
                log.info("Total: {}", loot.total());

                if (id != OwnHomeData.id()) {
                    // Apply raid penalty
                    if (sessionData.getTownHallLevel() == 0) {
                        log.warn("User town hall level not set, can't calculate loot penalty.");
                    } else {
                        int penalty = lootCalculator.getLevelPenalty(sessionData.getTownHallLevel(), townHallLevel);
                        if (penalty != 100) {
                            log.info("After penalty of {}%: {}", 100 - penalty, loot.total().percent(penalty));
                        }
                    }
                }

                // Save the stats in the session.
                if (clanName != null) {
                    // We keep the collection of stats for each clan in a a map from village ID -> stats
                    String statKey = CLAN_STATS_PREFIX + clanName;
                    Map<Long, VillageStats> clanStats = (Map<Long, VillageStats>) sessionData.getAttribute(statKey);
                    if (clanStats == null) {
                        clanStats = new HashMap<>();
                        sessionData.setAttribute(statKey, clanStats);
                    }
                    clanStats.put(userId, villageStats);
                }

            } catch (RuntimeException | IOException e) {
                log.warn("Could not read village", e);
            }
        }
    }

    /**
     * Summarise collected stats on session end.
     */
    public static void logSession(ProxySession session) {
        // For each session attribute with a key that starts "clan.stats."
        session
            .getSessionData()
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

    // This is just a guess...
    private Point getLocation(int layout, Village.Building building) {
        switch (layout) {
            case 0:
                return building.lmx != null && building.lmy != null ? new Point(building.lmx, building.lmy) : null;

            case 1:
                return building.l1x != null && building.l1y != null ? new Point(building.l1x, building.l1y) : null;

            case 2:
                return building.l2x != null && building.l2y != null ? new Point(building.l2x, building.l2y) : null;

            case 3:
                return building.l3x != null && building.l3y != null ? new Point(building.l3x, building.l3y) : null;

            case 4:
                return building.l4x != null && building.l4y != null ? new Point(building.l4x, building.l4y) : null;

            case 5:
                return building.l5x != null && building.l5y != null ? new Point(building.l5x, building.l5y) : null;

            default:
                throw new IllegalArgumentException("" + layout);
        }
    }
}