package sir.barchable.clash.model.json;

import sir.barchable.clash.model.Unit;

/**
 * @author Sir Barchable
 *         Date: 13/05/15
 */
public class Replay {
    public Village level;
    public WarVillage attacker;
    public WarVillage defender;

    public int prep_skip;
    public int end_tick;
    public Exec[] cmd;

    public static class Exec {
        public int ct;
        public Command c;
    }

    public static class Command {
        // 607/608
        public int t;
        // 608
        public WarVillage.Building[] bu;
        public WarVillage.Building[] tr;
        public Unit[] au;
        // 600/601/604
        public Timestamp base;
        public int d;
        public int x;
        public int y;

        public static class Timestamp {
            public int t;
        }
    }
}
