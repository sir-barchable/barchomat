package sir.barchable.clash.model;

import static java.lang.Math.min;

/**
 * Encapsulates logic for loot calculations.
 *
 * @author Sir Barchable
 *         Date: 23/04/15
 */
public class LootCalculator {
    private static final int[] PENALTIES = {100, 90, 50, 25, 5};

    private Logic logic;

    public LootCalculator(Logic logic) {
        this.logic = logic;
    }

    public LootCollection calculateAvailableLoot(LootCollection loot, int level) {
        return new LootCollection(
            calculateAvailableCollectorLoot(loot.collectorLoot, level),
            calculateAvailableStorageLoot(loot.storageLoot, level),
            calculateAvailableCastleLoot(loot.castleLoot, level),
            calculateAvailableTownHallLoot(loot.townHallLoot, level)
        );
    }

    /**
     * Get the raiding loot penalty for a given town hall level difference.
     *
     * @param level attacker's level
     * @param enemyLevel enemy level
     * @return the loot penalty percentage multiplier (applied after resource cap calculations)
     */
    public int getLevelPenalty(int level, int enemyLevel) {
        int delta = level - enemyLevel;
        if (delta < 0) {
            delta = 0;
        }
        return PENALTIES[min(delta, PENALTIES.length - 1)];
    }

    public Loot calculateAvailableCollectorLoot(Loot collectorLoot, int level) {
        int rPercent = logic.getInt("globals:RESOURCE_PRODUCTION_LOOT_PERCENTAGE", "NumberValue");
        int dePercent = logic.getInt("globals:RESOURCE_PRODUCTION_LOOT_PERCENTAGE_DARK_ELIXIR", "NumberValue");

        return new Loot(
            collectorLoot.getGold() * rPercent / 100,
            collectorLoot.getElixir() * rPercent / 100,
            collectorLoot.getDarkElixir() * dePercent / 100
        );
    }

    public Loot calculateAvailableStorageLoot(Loot storageLoot, int level) {
        int rPercent = logic.getInt("townhall_levels:" + level, "ResourceStorageLootPercentage");
        int rCap = logic.getInt("townhall_levels:" + level, "ResourceStorageLootCap");
        int dePercent = logic.getInt("townhall_levels:" + level, "DarkElixirStorageLootPercentage");
        int deCap = logic.getInt("townhall_levels:" + level, "DarkElixirStorageLootCap");

        return new Loot(
            min(storageLoot.getGold() * rPercent / 100, rCap),
            min(storageLoot.getElixir() * rPercent / 100, rCap),
            min(storageLoot.getDarkElixir() * dePercent / 100, deCap)
        );
    }

    public Loot calculateAvailableCastleLoot(Loot castleLoot, int level) {
        int rPercent = logic.getInt("globals:WAR_LOOT_PERCENTAGE", "NumberValue");

        return new Loot(
            castleLoot.getGold() * rPercent / 100,
            castleLoot.getElixir() * rPercent / 100,
            castleLoot.getDarkElixir() * rPercent / 100
        );
    }

    public Loot calculateAvailableTownHallLoot(Loot townHallLoot, int level) {
        return new Loot(
            townHallLoot.getGold(),
            townHallLoot.getElixir(),
            townHallLoot.getDarkElixir()
        );
    }

    public static class LootCollection {
        private Loot collectorLoot = Loot.ZERO;
        private Loot storageLoot = Loot.ZERO;
        private Loot castleLoot = Loot.ZERO;
        private Loot townHallLoot = Loot.ZERO;

        public LootCollection() { }

        public LootCollection(Loot collectorLoot, Loot storageLoot, Loot castleLoot, Loot townHallLoot) {
            this.collectorLoot = collectorLoot;
            this.storageLoot = storageLoot;
            this.castleLoot = castleLoot;
            this.townHallLoot = townHallLoot;
        }

        public LootCollection withCollectorLoot(Loot loot) {
            return new LootCollection(loot, storageLoot, castleLoot, townHallLoot);
        }

        public LootCollection withStorageLoot(Loot loot) {
            return new LootCollection(collectorLoot, loot, castleLoot, townHallLoot);
        }

        public LootCollection withCastleLoot(Loot loot) {
            return new LootCollection(collectorLoot, storageLoot, loot, townHallLoot);
        }

        public LootCollection withTownHallLoot(Loot loot) {
            return new LootCollection(collectorLoot, storageLoot, castleLoot, loot);
        }

        public Loot getCollectorLoot() {
            return collectorLoot;
        }

        public Loot getStorageLoot() {
            return storageLoot;
        }

        public Loot getCastleLoot() {
            return castleLoot;
        }

        public Loot getTownHallLoot() {
            return townHallLoot;
        }

        public Loot total() {
            return collectorLoot
                .add(storageLoot)
                .add(castleLoot)
                .add(townHallLoot);
        }

        public boolean isEmpty() {
            return total().equals(Loot.ZERO);
        }
    }

    public static class Loot {
        public static final Loot ZERO = new Loot();

        private int gold;
        private int elixir;
        private int darkElixir;

        public Loot() { }

        public Loot(int gold, int elixir, int darkElixir) {
            this.gold = gold;
            this.elixir = elixir;
            this.darkElixir = darkElixir;
        }

        public int getGold() {
            return gold;
        }

        public int getElixir() {
            return elixir;
        }

        public int getDarkElixir() {
            return darkElixir;
        }

        public Loot add(Loot addend) {
            return new Loot(
                gold + addend.gold,
                elixir + addend.elixir,
                darkElixir + addend.darkElixir
            );
        }

        public Loot addGold(int gold) {
            return new Loot(this.gold + gold, elixir, darkElixir);
        }

        public Loot addElixir(int elixir) {
            return new Loot(gold, this.elixir + elixir, darkElixir);
        }

        public Loot addDarkElixir(int darkElixir) {
            return new Loot(gold, elixir, this.darkElixir + darkElixir);
        }

        public Loot subtract(Loot subtrahend) {
            return new Loot(
                gold - subtrahend.gold,
                elixir - subtrahend.elixir,
                darkElixir - subtrahend.darkElixir
            );
        }

        public Loot percent(int percent) {
            return new Loot(
                gold * percent / 100,
                elixir * percent / 100,
                darkElixir * percent / 100
            );
        }

        public boolean isEmpty() {
            return equals(ZERO);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Loot loot = (Loot) o;

            if (gold != loot.gold) return false;
            if (elixir != loot.elixir) return false;
            return darkElixir == loot.darkElixir;

        }

        @Override
        public int hashCode() {
            int result = gold;
            result = 31 * result + elixir;
            result = 31 * result + darkElixir;
            return result;
        }

        @Override
        public String toString() {
            return "Loot[" + "g=" + gold + ", e=" + elixir + ", de=" + darkElixir + ']';
        }
    }

    public static class LootStorage {
        private String type;
        private int capacity;
        private int count;

        public LootStorage(String type, int capacity) {
            this.type = type;
            this.capacity = capacity;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "LootStorage[" + "type='" + type + '\'' + ", capacity=" + capacity + ", count=" + count + ']';
        }
    }
}
