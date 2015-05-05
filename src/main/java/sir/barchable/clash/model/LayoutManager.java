package sir.barchable.clash.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.server.LogicException;

import java.io.IOException;

/**
 * Layout manipulation for village descriptors.
 *
 * @author Sir Barchable
 */
public class LayoutManager {
    private static final Logger log = LoggerFactory.getLogger(LayoutManager.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Update coordinates in the village JSON description to the war layout.
     *
     * @param villageJson the village description
     * @return the village, with the layout set to the war layout
     */
    public String setWarLayout(String villageJson) throws IOException {
        System.out.println(villageJson);
        Village village = objectMapper.readValue(villageJson, Village.class);
        Integer layout = village.war_layout;
        if (layout != null) {
            try {
                setLayout(village, layout);
            } catch (LogicException e) {
                log.warn("Could not set layout to " + layout);
            }
        }
        return objectMapper.writeValueAsString(village);
    }

    /**
     * Update a village to the specified layout.
     *
     * @param village the village to update
     * @param layout the layout to use
     * @return the input village
     */
    public Village setLayout(Village village, int layout) {
        for (Village.Building building : village.buildings) {
            Point loc = getLocation(building, layout);
            if (loc != null) {
                building.x = loc.getX();
                building.y = loc.getY();
            } else {
                throw new LogicException("Missing coordinates in layout " + layout);
            }
        }
        return village;
    }

    private Point getLocation(Village.Building building, int layout) {
        switch (layout) {
            case 0:
                return building.lmx != null && building.lmy != null ? new Point(building.lmx, building.lmy) : null;

            case 1:
                return building.l1x != null && building.l1y != null ? new Point(building.l1x, building.l1y) : null;

            case 2:
                return building.l2x != null && building.l2y != null ? new Point(building.l2x, building.l2y) : null;

            case 3:
                return building.l3x != null && building.l3y != null ? new Point(building.l3x, building.l3y) : null;

            case 4:
                return building.l4x != null && building.l4y != null ? new Point(building.l4x, building.l4y) : null;

            case 5:
                return building.l5x != null && building.l5y != null ? new Point(building.l5x, building.l5y) : null;

            default:
                throw new IllegalArgumentException("" + layout);
        }
    }

    private Boolean getAirMode(Village.Building building, int layout) {
        switch (layout) {
            case 0:
                return building.air_mode;

            case 1:
                return building.air_mode_war;

            case 2:
                return building.air_mode2;

            case 3:
                return building.air_mode3;

            case 4:
                return building.air_mode4;

            case 5:
                return building.air_mode5;

            default:
                throw new IllegalArgumentException("" + layout);
        }
    }

    private Boolean getAttackMode(Village.Building building, int layout) {
        switch (layout) {
            case 0:
                return building.attack_mode;

            case 1:
                return building.attack_mode_war;

            case 2:
                return building.attack_mode2;

            case 3:
                return building.attack_mode3;

            case 4:
                return building.attack_mode4;

            case 5:
                return building.attack_mode5;

            default:
                throw new IllegalArgumentException("" + layout);
        }
    }

    private Integer getAimAngle(Village.Building building, int layout) {
        switch (layout) {
            case 0:
                return building.aim_angle;

            case 1:
                return building.aim_angle_war;

            case 2:
                return building.aim_angle2;

            case 3:
                return building.aim_angle3;

            case 4:
                return building.aim_angle4;

            case 5:
                return building.aim_angle5;

            default:
                throw new IllegalArgumentException("" + layout);
        }
    }
}
