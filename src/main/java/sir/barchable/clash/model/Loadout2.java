package sir.barchable.clash.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A human readable army model.
 *
 * @author Sir Barchable
 *         Date: 6/05/15
 */
public class Loadout2 {
    private String name;
    private Integer queen;
    private Integer king;
    private LoadoutUnit[] army;
    private LoadoutUnit[] spells;
    private LoadoutUnit[] garrison;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQueen() {
        return queen;
    }

    public void setQueen(Integer queen) {
        this.queen = queen;
    }

    public Integer getKing() {
        return king;
    }

    public void setKing(Integer king) {
        this.king = king;
    }

    public LoadoutUnit[] getArmy() {
        return army;
    }

    public void setArmy(LoadoutUnit[] army) {
        this.army = army;
    }

    public LoadoutUnit[] getSpells() {
        return spells;
    }

    public void setSpells(LoadoutUnit[] spells) {
        this.spells = spells;
    }

    public LoadoutUnit[] getGarrison() {
        return garrison;
    }

    public void setGarrison(LoadoutUnit[] garrison) {
        this.garrison = garrison;
    }

    /**
     * A more human readable unit model for army specs.
     *
     * @author Sir Barchable
     *         Date: 6/05/15
     */
    public static class LoadoutUnit {
        private String name;
        private int level;
        private int count;

        public LoadoutUnit(@JsonProperty("name") String name, @JsonProperty("level") int level, @JsonProperty("count") int count) {
            this.name = name;
            this.level = level;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public int getLevel() {
            return level;
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return "LoadoutUnit[" + "name='" + name + '\'' + ", level=" + level + ", count=" + count + ']';
        }
    }
}
