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
        double rPercent = logic.getInt("globals:RESOURCE_PRODUCTION_LOOT_PERCENTAGE", "NumberValue");
        double dePercent = logic.getInt("globals:RESOURCE_PRODUCTION_LOOT_PERCENTAGE_DARK_ELIXIR", "NumberValue");

        return new Loot(
            (int) (collectorLoot.getGold() * rPercent / 100),
            (int) (collectorLoot.getElixir() * rPercent / 100),
            (int) (collectorLoot.getDarkElixir() * dePercent / 100)
        );
    }

    public Loot calculateAvailableStorageLoot(Loot collectorLoot, int level) {
        double rPercent = logic.getInt("townhall_levels:" + level, "ResourceStorageLootPercentage");
        int rCap = logic.getInt("townhall_levels:" + level, "ResourceStorageLootCap");
        double dePercent = logic.getInt("townhall_levels:" + level, "DarkElixirStorageLootPercentage");
        int deCap = logic.getInt("townhall_levels:" + level, "DarkElixirStorageLootCap");

        return new Loot(
            min((int) (collectorLoot.getGold() * rPercent / 100), rCap),
            min((int) (collectorLoot.getElixir() * rPercent / 100), rCap),
            min((int) (collectorLoot.getDarkElixir() * dePercent / 100), deCap)
        );
    }


    public Loot calculateAvailableCastleLoot(Loot castleLoot, int level) {
        // Castle loot percentages are half of storage percentages
        double rPercent = logic.getInt("townhall_levels:" + level, "ResourceStorageLootPercentage") / 2;
        double dePercent = logic.getInt("townhall_levels:" + level, "DarkElixirStorageLootPercentage") / 2;

        return new Loot(
             (int) (castleLoot.getGold() * rPercent / 100),
             (int) (castleLoot.getElixir() * rPercent / 100),
             (int) (castleLoot.getDarkElixir() * dePercent / 100)
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
        private Loot collectorLoot;
        private Loot storageLoot;
        private Loot castleLoot;
        private Loot townHallLoot;

        public LootCollection(Loot collectorLoot, Loot storageLoot, Loot castleLoot, Loot townHallLoot) {
            this.collectorLoot = collectorLoot;
            this.storageLoot = storageLoot;
            this.castleLoot = castleLoot;
            this.townHallLoot = townHallLoot;
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
    }

    public static class Loot {
        private int gold;
        private int elixir;
        private int darkElixir;

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

        public Loot percent(int percent) {
            return new Loot(
                gold * percent / 100,
                elixir * percent / 100,
                darkElixir * percent / 100
            );
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Loot[");
            sb.append("g=").append(gold);
            sb.append(", e=").append(elixir);
            sb.append(", de=").append(darkElixir);
            sb.append(']');
            return sb.toString();
        }
    }
}
