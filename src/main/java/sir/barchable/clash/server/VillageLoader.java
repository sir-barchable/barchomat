package sir.barchable.clash.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.*;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.proxy.MessageSaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import static sir.barchable.clash.protocol.Pdu.Type.EnemyHomeData;

/**
 * Load saved villages from the specified directory. Villages are typically saved by a
 * {@link MessageSaver} hooked into the proxy when the {@code -s} save flag is passed on startup.
 *
 * @author Sir Barchable
 */
public class VillageLoader {
    private static final Logger log = LoggerFactory.getLogger(VillageLoader.class);
    private static final Pattern HOME_PATTERN = Pattern.compile("OwnHomeData.*\\.pdu");
    private static final Pattern ENEMY_HOME_PATTERN = Pattern.compile("EnemyHomeData.*\\.pdu");
    private static final Pattern VISITED_HOME_PATTERN = Pattern.compile("VisitedHomeData.*\\.pdu");

    private LayoutManager layoutManager = new LayoutManager();

    private Logic logic;
    private MessageFactory messageFactory;
    private File home;
    private File enemyPrototype;
    private File[] enemyHomes;

    public VillageLoader(Logic logic, MessageFactory messageFactory, File dir) throws IOException {
        this.logic = logic;
        this.messageFactory = messageFactory;

        Optional<File> homeFile = Files.walk(dir.toPath())
            .map(Path::toFile)
            .filter(file -> HOME_PATTERN.matcher(file.getName()).matches())
            .findFirst();

        if (homeFile.isPresent()) {
            home = homeFile.get();
        } else {
            throw new FileNotFoundException("No home village file found in " + dir);
        }

        enemyPrototype = new File("EnemyHomeDataPrototype.pdu");

        enemyHomes = Files.walk(dir.toPath())
            .map(Path::toFile)
            .filter(file -> ENEMY_HOME_PATTERN.matcher(file.getName()).matches() || VISITED_HOME_PATTERN.matcher(file.getName()).matches())
            .toArray(File[]::new);
    }

    public File getHome() {
        return home;
    }

    /**
     * Load the user's home.
     */
    public Message loadHomeVillage() throws IOException {
        try (FileInputStream in = new FileInputStream(home)) {
            return messageFactory.fromStream(in);
        }
    }

    private Message loadEnemyPrototype() throws IOException {
        try (FileInputStream in = new FileInputStream(enemyPrototype)) {
            return messageFactory.fromStream(EnemyHomeData, in);
        }
    }

    /**
     * Load the nth saved enemy home. The index will be wrapped if it is longer than the array length.
     *
     * @param villageIndex the index of the home to load
     * @return the village, or null if there are no saved enemy villages
     */
    public Message loadEnemyVillage(int villageIndex) throws IOException {
        if (villageIndex < 0) {
            throw new IllegalArgumentException();
        }
        if (enemyHomes.length == 0) {
            return null;
        }
        File villageFile = enemyHomes[villageIndex % enemyHomes.length];
        try (FileInputStream in = new FileInputStream(villageFile)) {
            log.debug("loading village {}", villageFile);
            Message village = null;
            if (ENEMY_HOME_PATTERN.matcher(villageFile.getName()).matches()) {
                village = messageFactory.fromStream(in);
            } else if (VISITED_HOME_PATTERN.matcher(villageFile.getName()).matches()) {
                village = messageFactory.fromStream(in);
                Message homeVillage = loadHomeVillage();
                Message enemyVillage = loadEnemyPrototype();
                enemyVillage.set("userId", village.get("userId"));
                enemyVillage.set("homeVillage", layoutManager.setWarLayout(village.getString("homeVillage")));
                enemyVillage.set("user", village.get("user"));
                enemyVillage.set("resources", village.get("resources"));
                enemyVillage.set("attacker", homeVillage.get("user"));
                enemyVillage.set("attackerResources", homeVillage.get("resources"));
                village = enemyVillage;
            }
            return village;
        }
    }
}
