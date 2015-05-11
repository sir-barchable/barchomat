package sir.barchable.clash.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.LayoutManager;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.model.json.WarVillage;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.protocol.PduOutputStream;
import sir.barchable.clash.proxy.MessageSaver;
import sir.barchable.util.Json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import static sir.barchable.clash.protocol.Pdu.Type.EnemyHomeData;
import static sir.barchable.util.NoopCipher.NOOP_CIPHER;

/**
 * Load saved villages from the specified directory. Villages are typically saved by a
 * {@link MessageSaver} hooked into the proxy when the {@code -s} save flag is passed on startup.
 *
 * @author Sir Barchable
 */
public class VillageLoader {
    private static final Logger log = LoggerFactory.getLogger(VillageLoader.class);
    private static final Pattern VISITED_HOME_PATTERN = Pattern.compile("(Enemy|Visited|War)HomeData.*\\.pdu");

    private LayoutManager layoutManager = new LayoutManager();

    private Logic logic;
    private MessageFactory messageFactory;
    private File homeFile;
    private Village homeVillage;
    private Message ownHomeData;
    private File[] enemyHomes;

    public VillageLoader(Logic logic, MessageFactory messageFactory, File homeFile, File villageDir) throws IOException {
        this.logic = logic;
        this.messageFactory = messageFactory;

        this.homeFile = homeFile;
        try (FileInputStream in = new FileInputStream(homeFile)) {
            ownHomeData = messageFactory.fromStream(in);
            homeVillage = Json.valueOf(ownHomeData.getString("homeVillage"), Village.class);
        }

        enemyHomes = Files.walk(villageDir.toPath())
            .map(Path::toFile)
            .filter(file -> VISITED_HOME_PATTERN.matcher(file.getName()).matches())
            .toArray(File[]::new);
    }

    /**
     * Load the user's home.
     */
    public Message getOwnHomeData() throws IOException {
        String villageJson = Json.toString(homeVillage);
        ownHomeData.set("homeVillage", villageJson);
        ownHomeData.set("timeStamp", (int) (System.currentTimeMillis() / 1000));
        return ownHomeData;
    }

    public Village getHomeVillage() {
        return homeVillage;
    }

    /**
     * Create a new enemy village structure.
     */
    private Message newEnemyPrototype() throws IOException {
        Message village = messageFactory.newMessage(EnemyHomeData);
        village.set("timeStamp", (int) (System.currentTimeMillis() / 1000));
        village.set("age", 0);
        return village;
    }

    /**
     * Load the nth saved enemy home. The index will be wrapped if it is longer than the array length.
     *
     * @param villageIndex the index of the home to load
     * @param war load the war layout?
     * @return the village, or null if there are no saved enemy villages
     */
    public Message loadEnemyVillage(int villageIndex, boolean war) throws IOException {
        if (villageIndex < 0) {
            throw new IllegalArgumentException();
        }
        if (enemyHomes.length == 0) {
            return null;
        }
        File villageFile = enemyHomes[villageIndex % enemyHomes.length];
        try (FileInputStream in = new FileInputStream(villageFile)) {
            log.debug("loading village {}", villageFile);
            Message village = messageFactory.fromStream(in);

            // Convert if necessary
            switch (village.getType()) {
                case VisitedHomeData:
                    village = visitedHomeToEnemyHome(village, war);
                    break;

                case WarHomeData:
                    village = warHomeToEnemyHome(village);
                    break;
            }

            return village;
        }
    }

    private Message visitedHomeToEnemyHome(Message visitedVillage, boolean war) throws IOException {
        Message homeVillage = getOwnHomeData();
        Message enemyVillage = newEnemyPrototype();

        // Copy data from visited -> enemy
        enemyVillage.set("homeId", visitedVillage.get("homeId"));
        if (war) {
            // swap from home layout to war layout
            enemyVillage.set("homeVillage", Json.toString(
                layoutManager.setWarLayout(
                    layoutManager.loadVillage(
                        visitedVillage.getString("homeVillage")
                    )
                )
            ));
        }
        enemyVillage.set("user", visitedVillage.get("user"));
        enemyVillage.set("resources", visitedVillage.get("resources"));
        enemyVillage.set("attacker", homeVillage.get("user"));
        enemyVillage.set("attackerResources", homeVillage.get("resources"));
        return enemyVillage;
    }

    private Message warHomeToEnemyHome(Message village) throws IOException {
        Message homeVillage = getOwnHomeData();
        Message enemyVillage = newEnemyPrototype();
        enemyVillage.set("homeId", village.get("homeId"));

        WarVillage warVillage = layoutManager.loadWarVillage(village.getString("homeVillage"));
        enemyVillage.set("homeVillage", Json.toString(layoutManager.warVillageToVillage(warVillage)));

        //
        // The "user" in the war home data is the visiting user, not the enemy.
        // We reuse the existing user data structure as a surrogate for the missing enemy user data
        // and overwrite the fields we need for display with those that we can extract from the json
        // definition.
        //

        Map<String, Object> user = village.getStruct("user");
        user.put("userName", warVillage.name);
        Map<String, Object> clan = (Map<String, Object>) user.get("clan");
        if (clan != null) { // should always be non-null
            clan.put("clanName", warVillage.alliance_name);
            clan.put("badge", warVillage.badge_id);
        }
        enemyVillage.set("user", user);
        enemyVillage.set("resources", village.get("resources"));

        // Attacker values from home data
        enemyVillage.set("attacker", homeVillage.get("user"));
        enemyVillage.set("attackerResources", homeVillage.get("resources"));
        return enemyVillage;
    }

    /**
     * Save home village state.
     */
    public void save() {
        log.info("Saving " + homeFile);
        try (PduOutputStream out = new PduOutputStream(new FileOutputStream(homeFile), NOOP_CIPHER)) {
            out.write(messageFactory.toPdu(getOwnHomeData()));
        } catch (IOException e) {
            log.error("Couldn't save home village: " + e);
        }
    }
}
