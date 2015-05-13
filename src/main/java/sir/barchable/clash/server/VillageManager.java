package sir.barchable.clash.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.LayoutManager;
import sir.barchable.clash.model.Unit;
import sir.barchable.clash.model.json.Replay;
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
import java.util.*;
import java.util.regex.Pattern;

import static sir.barchable.util.NoopCipher.NOOP_CIPHER;

/**
 * Load saved villages from the specified directory. Villages are typically saved by a
 * {@link MessageSaver} hooked into the proxy when the {@code -s} save flag is passed on startup.
 *
 * @author Sir Barchable
 */
public class VillageManager {
    private static final Logger log = LoggerFactory.getLogger(VillageManager.class);
    private static final Pattern VISITED_HOME_PATTERN = Pattern.compile("(HomeBattleReplay|((Enemy|Visited|War)Home))Data.*\\.pdu");
    private LayoutManager layoutManager;
    private LoadoutManager loadoutManager;

    private MessageFactory messageFactory;
    private File homeFile;
    private Village homeVillage;
    private Message ownHomeData;
    private File[] enemyHomes;

    public VillageManager(MessageFactory messageFactory, LoadoutManager loadoutManager, File homeFile, File villageDir) throws IOException {
        this.messageFactory = messageFactory;
        this.loadoutManager = loadoutManager;
        this.layoutManager = new LayoutManager();

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
        try (FileInputStream in = new FileInputStream("EnemyHomeDataPrototype.pdu")) {
            return messageFactory.fromStream(in);
        }

//        Message village = messageFactory.newMessage(EnemyHomeData);
//        village.set("timeStamp", (int) (System.currentTimeMillis() / 1000));
//        village.set("age", 0);
//        return village;
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
                case EnemyHomeData:
                    village = filterEnemyHome(village, war);
                    break;

                case VisitedHomeData:
                    village = visitedHomeToEnemyHome(village, war);
                    break;

                case WarHomeData:
                    village = warHomeToEnemyHome(village);
                    break;

                case HomeBattleReplayData:
                    village = replayToEnemyHome(village);
                    break;
            }

            return village;
        }
    }

    private Message filterEnemyHome(Message enemyVillage, boolean war) throws IOException {
        Message homeVillage = getOwnHomeData();

        if (war) {
            setWarLayout(enemyVillage);
        }

        enemyVillage.set("attacker", homeVillage.get("user"));
        enemyVillage.set("attackerResources", homeVillage.get("resources"));
        return enemyVillage;
    }

    private Message visitedHomeToEnemyHome(Message visitedVillage, boolean war) throws IOException {
        Message homeVillage = getOwnHomeData();
        Message enemyVillage = newEnemyPrototype();

        // Copy data from visited -> enemy
        enemyVillage.set("homeId", visitedVillage.get("homeId"));
        enemyVillage.set("homeVillage", visitedVillage.get("homeVillage"));
        if (war) {
            setWarLayout(enemyVillage);
        }
        enemyVillage.set("user", visitedVillage.get("user"));
        enemyVillage.set("resources", visitedVillage.get("resources"));
        enemyVillage.set("attacker", homeVillage.get("user"));
        enemyVillage.set("attackerResources", homeVillage.get("resources"));
        return enemyVillage;
    }

    /**
     * Swap to war layout. Copies the war layout to the home layout and updates the village json.
     */
    private void setWarLayout(Message enemyVillage) throws IOException {
        // swap from home layout to war layout
        enemyVillage.set("homeVillage", Json.toString(
            layoutManager.setWarLayout(
                layoutManager.loadVillage(
                    enemyVillage.getString("homeVillage")
                )
            )
        ));
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

        Message user = village.getMessage("user");
        user.set("userName", warVillage.name);
        Message clan = messageFactory.newMessage("ClanComponent");
        user.set("clan", clan.getFields());
        clan.set("clanName", warVillage.alliance_name);
        clan.set("badge", warVillage.badge_id);
        enemyVillage.set("user", user.getFields());

        // Attacker values from home data
        enemyVillage.set("attacker", homeVillage.get("user"));
        enemyVillage.set("attackerResources", homeVillage.get("resources"));
        return enemyVillage;
    }

    private Message replayToEnemyHome(Message replayMessage) throws IOException {
        Replay replay = Json.valueOf(replayMessage.getString("replay"), Replay.class);
        Message homeVillage = getOwnHomeData();
        Message enemyVillage = newEnemyPrototype();
        WarVillage warVillage = replay.defender;

        Village village = replay.level;
        village.war = null;
        village.wave_num = null;

        //
        // Dig out the good bits from the 608 "hidden shit" command
        //

        WarVillage.Building[] teslas = null;
        WarVillage.Building[] traps = null;
        Unit[] garrison = null;

        for (Replay.Exec exec : replay.cmd) {
            if (exec.ct == 608) {
                teslas = exec.c.bu;
                traps = exec.c.tr;
                garrison = exec.c.au;
                break;
            }
        }
        layoutManager.setTraps(village, teslas, traps);
        loadoutManager.setGarrison(enemyVillage, garrison);

        long homeId = (long) warVillage.avatar_id_high << 32 | warVillage.avatar_id_low & 0xffffffffl;
        enemyVillage.set("homeId", homeId);
        enemyVillage.set("homeVillage", Json.toString(village));

        Message user = enemyVillage.getMessage("user");
        user.set("userName", warVillage.name);
        Message clan = messageFactory.newMessage("ClanComponent");
        user.set("clan", clan.getFields());
        clan.set("clanName", warVillage.alliance_name);
        clan.set("badge", warVillage.badge_id);
        user.set("castleLevel", 3);

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
