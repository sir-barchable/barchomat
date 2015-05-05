package sir.barchable.clash.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.ResourceException;
import sir.barchable.clash.model.LoadOut;
import sir.barchable.clash.model.Unit;
import sir.barchable.clash.protocol.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Load and apply army load-outs.
 *
 * @author Sir Barchable
 */
public class LoadoutManager {
    private static final Logger log = LoggerFactory.getLogger(LoadoutManager.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, LoadOut> loadOuts = new HashMap<>();

    public LoadoutManager(File dir) throws IOException {
        Files.walk(dir.toPath())
            .map(Path::toFile)
            .filter(file -> file.getName().endsWith(".army.json"))
            .forEach(file -> {
                try {
                    LoadOut loadOut = objectMapper.readValue(file, LoadOut.class);
                    loadOuts.put(loadOut.getName(), loadOut);
                    log.info("Loaded army '{}' from {}", loadOut.getName(), file.getName());
                } catch (IOException e) {
                    throw new ResourceException(e);
                }
            });
    }

    /**
     * Get the loadout with the specified name
     */
    public LoadOut getLoadout(String name) {
        LoadOut loadout = loadOuts.get(name);
        if (loadout == null) {
            throw new IllegalArgumentException("No loadout with name " + name);
        }
        return loadout;
    }

    public LoadOut loadLoadOut(File loadoutFile) throws IOException {
        return objectMapper.readValue(loadoutFile, LoadOut.class);
    }

    public void applyLoadOut(Message village, String loadoutName) {
        Map<String, Object> resources = village.getStruct("resources");
        if (resources == null) {
            throw new IllegalArgumentException("Incomplete village definition (no resources)");
        }

        Unit[] units = getLoadout(loadoutName).getUnits();
        Arrays.sort(units, (o1, o2) -> o1.getId() - o2.getId());

        resources.put(
            "unitCounts",
            Arrays.stream(units).map(unit -> newResource(unit, Unit::getCnt)).toArray())
        ;

        resources.put(
            "unitLevels",
            Arrays.stream(units).map(unit -> newResource(unit, Unit::getLvl)).toArray()
        );
    }

    private Map<String, Object> newResource(Unit unit, Function<Unit, Integer> property) {
        Map<String, Object> resourceCount = new LinkedHashMap<>();
        resourceCount.put("type", unit.getId());
        resourceCount.put("value", property.apply(unit));
        return resourceCount;
    }
}
