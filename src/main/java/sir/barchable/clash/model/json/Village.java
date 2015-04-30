package sir.barchable.clash.model.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * POJO for parsing home village JSON from OwnHomeData/EnemyHomeData/VisitedHomeData messages.
 *
 * @author Sir Barchable
 *         Date: 17/04/15
 */

@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(NON_NULL)
public class Village {
    public Integer wave_num;
    public Boolean android_client;

    public Integer active_layout;
    public Integer war_layout;
    public Integer[] layout_state;
    public Building[] buildings;
    public Building[] obstacles;
    public Building[] traps;
    public Building[] decos;

    public Integer cooldown;
    public CoolDown[] cooldowns;

    @JsonAutoDetect(fieldVisibility = ANY)
    @JsonInclude(NON_NULL)
    public static class CoolDown {
        public Integer cooldown;
        public Integer target;
    }

    public Integer[] newShopBuildings;
    public Integer[] newShopTraps;
    public Integer[] newShopDecos;

    public RespawnVars respawnVars;

    public Integer last_league_rank;
    public Integer last_alliance_level;
    public Integer last_league_shuffle;
    public Integer last_news_seen;
    public Boolean edit_mode_shown;
    public String troop_req_msg;
    public String war_req_msg;
    public Integer war_tutorials_seen;
    public Boolean war_base;
    public Boolean help_opened;
    public Boolean bool_layout_edit_shown_erase;

    @JsonAutoDetect(fieldVisibility = ANY)
    @JsonInclude(NON_NULL)
    public static class Building {
        public Integer data;
        public Integer lvl;
        public Integer hp;
        public Boolean reg;
        public Boolean needs_repair;
        public Boolean locked;
        public Integer share_replay_time;

        public Integer x, y;

        public Integer lmx, lmy;
        public Integer l1x, l1y;
        public Integer l2x, l2y;
        public Integer l3x, l3y;
        public Integer l4x, l4y;
        public Integer l5x, l5y;

        public Integer emx, emy;
        public Integer e1x, e1y;
        public Integer e2x, e2y;
        public Integer e3x, e3y;
        public Integer e4x, e4y;
        public Integer e5x, e5y;

        public Integer boost_t;
        public Boolean boost_pause;

        public Integer clear_t;

        public Integer res_time;
        public Integer const_t, const_t_end;

        public Integer storage_type;
        public Integer loot_multiply_ver;

        public Boolean attack_mode;
        public Boolean attack_mode_draft;
        public Boolean attack_mode_war;
        public Boolean attack_mode_draft_war;
        public Boolean attack_mode2;
        public Boolean attack_mode_d2;
        public Boolean attack_mode3;
        public Boolean attack_mode_d3;
        public Boolean attack_mode4;
        public Boolean attack_mode_d4;
        public Boolean attack_mode5;
        public Boolean attack_mode_d5;
        public Boolean ammo;

        public Boolean air_mode;
        public Boolean air_mode_draft;
        public Boolean air_mode_war;
        public Boolean air_mode_draft_war;
        public Boolean air_mode2;
        public Boolean air_mode_d2;
        public Boolean air_mode3;
        public Boolean air_mode_d3;
        public Boolean air_mode4;
        public Boolean air_mode_d4;
        public Boolean air_mode5;
        public Boolean air_mode_d5;

        public Integer[][] units;
        public Integer unit_req_time;
        public Integer clan_mail_time;
        public Unit unit_prod;
        public Unit unit_upg;
        public Unit hero_upg;

        @JsonAutoDetect(fieldVisibility = ANY)
        @JsonInclude(NON_NULL)
        public static class Unit {
            public Integer unit_type;
            public Integer level;
            public Integer t, t_end;
            public Integer id;
            public Slot[] slots;

            @JsonAutoDetect(fieldVisibility = ANY)
            static class Slot {
                public int id;
                public int cnt;
            }
        }
    }

    @JsonAutoDetect(fieldVisibility = ANY)
    public static class RespawnVars {
        public int secondsFromLastRespawn;
        public int respawnSeed;
        public int obstacleClearCounter;
        public int time_to_gembox_drop;
        public int time_in_gembox_period;
    }
}
