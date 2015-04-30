package sir.barchable.clash;

/**
 * @author Sir Barchable
 *         Date: 27/04/15
 */
public class VillageStats {
    private String userName;
    private Defense defense;

    public VillageStats(String userName, Defense defense) {
        this.userName = userName;
        this.defense = defense;
    }

    public String getUserName() {
        return userName;
    }

    public Defense getDefense() {
        return defense;
    }

    public static class Defense {
        private int hp;
        private int wallHp;
        private int dps;

        public Defense() { }

        public Defense(int hp, int wallHp, int dps) {
            this.hp = hp;
            this.wallHp = wallHp;
            this.dps = dps;
        }

        public int getHp() {
            return hp;
        }

        public int getDps() {
            return dps;
        }

        public Defense add(Defense addend) {
            return new Defense(hp + addend.hp, wallHp + addend.wallHp, dps + addend.dps);
        }

        @Override
        public String toString() {
            return "Defense[" + "hp=" + hp + ", wallHp=" + wallHp + ", dps=" + dps + ']';
        }
    }
}
