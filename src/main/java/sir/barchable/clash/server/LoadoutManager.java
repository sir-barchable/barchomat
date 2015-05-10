package sir.barchable.clash.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.ResourceException;
import sir.barchable.clash.model.Army;
import sir.barchable.clash.model.Loadout;
import sir.barchable.clash.model.Loadout.LoadoutUnit;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.Unit;
import sir.barchable.clash.protocol.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 * Load and apply army load-outs.
 *
 * @author Sir Barchable
 */
public class LoadoutManager {
    private static final Logger log = LoggerFactory.getLogger(LoadoutManager.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    private Logic logic;
    private Map<String, Army> armies = new HashMap<>();

    public LoadoutManager(Logic logic, File dir) throws IOException {
        this.logic = logic;

        Files.walk(dir.toPath())
            .map(Path::toFile)
            .filter(file -> file.getName().endsWith(".army.json"))
            .forEach(this::addArmy);

        Files.walk(dir.toPath())
            .map(Path::toFile)
            .filter(file -> file.getName().endsWith(".loadout.json"))
            .forEach(this::addLoadout);
    }

    public void addArmy(File file) {
        try {
            Army army = objectMapper.readValue(file, Army.class);
            if (armies.put(army.getName(), army) != null) {
                throw new ResourceException("Duplicate loadout " + army.getName());
            }
            log.info("Read army '{}' from {}", army.getName(), file.getName());
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    public void addLoadout(File file) {
        try {
            Loadout loadout = objectMapper.readValue(file, Loadout.class);
            if (armies.put(loadout.getName(), toArmy(loadout)) != null) {
                throw new ResourceException("Duplicate loadout " + loadout.getName());
            }

            log.info("Read loadout '{}' from {}", loadout.getName(), file.getName());
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    /**
     * Turn the human readable loadout model into out internal army model.
     */
    private Army toArmy(Loadout loadout) {
        Army army = new Army();
        List<Unit> units = new ArrayList<>();
        List<Unit> spells = new ArrayList<>();
        List<Unit> heroes = new ArrayList<>();
        List<Unit> garrison = new ArrayList<>();

        if (loadout.getArmy() != null) {
            for (LoadoutUnit loadoutUnit : loadout.getArmy()) {
                Unit unit = toUnit(loadoutUnit);
                logic.assertType("characters", unit.getId());
                units.add(unit);
            }
        }

        if (loadout.getSpells() != null) {
            for (LoadoutUnit loadoutUnit : loadout.getSpells()) {
                Unit unit = toUnit(loadoutUnit);
                logic.assertType("spells", unit.getId());
                spells.add(unit);
            }
        }

        Integer king = loadout.getKing();
        if (king != null) {
            heroes.add(new Unit(28000000, 1, king - 1));
        }

        Integer queen = loadout.getQueen();
        if (queen != null) {
            heroes.add(new Unit(28000001, 1, queen - 1));
        }

        if (loadout.getGarrison() != null) {
            for (LoadoutUnit loadoutUnit : loadout.getGarrison()) {
                Unit unit = toUnit(loadoutUnit);
                logic.assertType("characters", unit.getId());
                garrison.add(unit);
            }
        }

        army.setUnits(units.toArray(new Unit[units.size()]));
        army.setSpells(spells.toArray(new Unit[spells.size()]));
        army.setHeroes(heroes.toArray(new Unit[heroes.size()]));
        army.setGarrison(garrison.toArray(new Unit[garrison.size()]));

        return army;
    }

    private Unit toUnit(LoadoutUnit unit) {
        return new Unit(logic.getTypeId("characters", unit.getName()), unit.getCount(), unit.getLevel() - 1);
    }

    /**
     * Get the loadout with the specified name
     */
    public Army getLoadout(String name) {
        Army loadout = armies.get(name);
        if (loadout == null) {
            throw new IllegalArgumentException("No loadout with name " + name);
        }
        return loadout;
    }

    public Army loadLoadOut(File loadoutFile) throws IOException {
        return objectMapper.readValue(loadoutFile, Army.class);
    }

    public void applyLoadOut(Message village, String loadoutName) {
        Map<String, Object> resources = village.getStruct("attackerResources");
        if (resources == null) {
            throw new IllegalArgumentException("Incomplete village definition (no resources)");
        }

        Army army = getLoadout(loadoutName);

        Unit[] units = army.getUnits();
        Arrays.sort(units, (o1, o2) -> o1.getId() - o2.getId());
        resources.put("unitCounts", toResources(units, Unit::getCnt));
        resources.put("unitLevels", toResources(units, Unit::getLvl));
        int totalSpaces = 0;
        for (Unit unit : units) {
            totalSpaces += unit.getCnt() * logic.getInt(unit.getId(), "HousingSpace");
        }

        Unit[] spells = army.getSpells();
        Arrays.sort(spells, (o1, o2) -> o1.getId() - o2.getId());
        resources.put("spellCounts", toResources(spells, Unit::getCnt));
        resources.put("spellLevels", toResources(spells, Unit::getLvl));

        Unit[] heroes = army.getHeroes();
        Arrays.sort(heroes, (o1, o2) -> o1.getId() - o2.getId());
        resources.put("heroLevels", toResources(heroes, Unit::getLvl));
        resources.put("heroHealth", toResources(heroes, 0));
        resources.put("heroState", toResources(heroes, 3));

        Unit[] garrison = army.getGarrison();
        Arrays.sort(garrison, (o1, o2) -> o1.getId() - o2.getId());
        resources.put("allianceUnits", toUnitComponents(garrison));

        int garrisonSpaces = 0;
        for (Unit unit : garrison) {
            garrisonSpaces += unit.getCnt() * logic.getInt(unit.getId(), "HousingSpace");
        }
        log.info("Applied loadout {}, army size={}, garrison size={}", loadoutName, totalSpaces, garrisonSpaces);
    }

    private Object toUnitComponents(Unit[] units) {
        return Arrays.stream(units).map(this::newUnitComponent).toArray();
    }

    private Map<String, Object> newUnitComponent(Unit unit) {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("typeId", unit.getId());
        component.put("level", unit.getLvl());
        component.put("count", unit.getCnt());
        return component;
    }

    private Object[] toResources(Unit[] units, Function<Unit, Integer> prop) {
        return Arrays.stream(units).map(unit -> newResourceComponent(unit.getId(), prop.apply(unit))).toArray();
    }

    private Object[] toResources(Unit[] units, int value) {
        return Arrays.stream(units).map(unit -> newResourceComponent(unit.getId(), value)).toArray();
    }

    private Map<String, Object> newResourceComponent(int unitId, int count) {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("type", unitId);
        component.put("value", count);
        return component;
    }

    public boolean contains(String loadout) {
        return armies.containsKey(loadout);
    }
}
