package sir.barchable.clash.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Troop specs for load-outs and war village json.
 *
 * @author Sir Barchable
 *         Date: 5/05/15
 */

public class Unit {
    int id;
    int cnt;
    int lvl;

    public Unit(@JsonProperty("id") int id, @JsonProperty("cnt") int cnt, @JsonProperty("lvl") int lvl) {
        this.id = id;
        this.cnt = cnt;
        this.lvl = lvl;
    }

    public int getId() {
        return id;
    }

    public int getCnt() {
        return cnt;
    }

    public int getLvl() {
        return lvl;
    }
}
