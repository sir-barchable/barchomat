package sir.barchable.clash.model;

/**
 * Army spec.
 *
 * @author Sir Barchable
 *         Date: 5/05/15
 */
public class LoadOut {
    private String name;

    private Unit[] units;
    private Unit[] spells;
    private Unit[] heroes;
    private Unit[] garrison;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Unit[] getUnits() {
        return units;
    }

    public void setUnits(Unit[] units) {
        this.units = units;
    }

    public Unit[] getSpells() {
        return spells;
    }

    public void setSpells(Unit[] spells) {
        this.spells = spells;
    }

    public Unit[] getHeroes() {
        return heroes;
    }

    public void setHeroes(Unit[] heroes) {
        this.heroes = heroes;
    }

    public Unit[] getGarrison() {
        return garrison;
    }

    public void setGarrison(Unit[] garrison) {
        this.garrison = garrison;
    }
}
