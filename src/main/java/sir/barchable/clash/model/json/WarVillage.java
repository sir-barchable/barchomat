package sir.barchable.clash.model.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import sir.barchable.clash.model.Unit;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * POJO for parsing village JSON from WarHomeData messages.
 *
 * @author Sir Barchable
 */

@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(NON_NULL)
public class WarVillage {
    public Building[] buildings;
    public Building[] obstacles;
    public Building[] traps;
    public Building[] decos;

    public int avatar_id_high;
    public int avatar_id_low;
    public String name;
    public String alliance_name;
    public int xp_level;
    public int badge_id;
    public int alliance_exp_level;
    public int league_type;
    public int castle_lvl;
    public int castle_total;
    public int castle_used;
    public int town_hall_lvl;
    public int score;
    public int alliance_unit_visit_capacity;

    public Resource[] resources;
    public Unit[] alliance_units;
    public Resource[] hero_states;
    public Resource[] hero_health;
    public Resource[] hero_upgrade;

    @JsonAutoDetect(fieldVisibility = ANY)
    @JsonInclude(NON_NULL)
    public static class Resource {
        public int id;
        public int cnt;
    }

    @JsonAutoDetect(fieldVisibility = ANY)
    @JsonInclude(NON_NULL)
    public static class Building {
        public Integer data;
        public Integer lvl;
        public Integer hp;
        public Boolean reg;

        public Integer x, y;

        public Integer const_t, const_t_end;

        public Boolean attack_mode;
        public Boolean attack_mode_war;
        public Boolean attack_mode2;
        public Boolean attack_mode3;
        public Boolean attack_mode4;
        public Boolean attack_mode5;
        public Boolean ammo;

        public Boolean air_mode;
        public Boolean air_mode_war;
        public Boolean air_mode2;
        public Boolean air_mode3;
        public Boolean air_mode4;
        public Boolean air_mode5;

        public Boolean aim_angle;
        public Boolean aim_angle_war;
        public Boolean aim_angle2;
        public Boolean aim_angle3;
        public Boolean aim_angle4;
        public Boolean aim_angle5;

        public Upg hero_upg;

        @JsonAutoDetect(fieldVisibility = ANY)
        @JsonInclude(NON_NULL)
        public static class Upg {
            public Integer level;
            public Integer t;
        }
    }
}
