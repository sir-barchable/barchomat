package sir.barchable.clash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.LootCalculator.Loot;
import sir.barchable.clash.model.LootCalculator.LootCollection;
import sir.barchable.clash.model.SessionState;
import sir.barchable.clash.protocol.Pdu;
import sir.barchable.clash.proxy.MessageTap;
import sir.barchable.clash.proxy.ProxySession;

import java.util.HashMap;
import java.util.Map;

/**
 * What did an attack cost?
 *
 * @author Sir Barchable
 *         Date: 11/05/15
 */
public class AttackAnalyzer implements MessageTap {
    private static final Logger log = LoggerFactory.getLogger(AttackAnalyzer.class);

    private Logic logic;
    private VillageAnalyzer villageAnalyzer;

    public AttackAnalyzer(Logic logic) {
        this.logic = logic;
        this.villageAnalyzer = new VillageAnalyzer(logic);
    }

    @Override
    public void onMessage(Pdu.Type type, Map<String, Object> message) {
        switch (type) {
            case EnemyHomeData:
                // Set up for attack
                setup((Map<String, Object>) message.get("attackerResources"));
                break;

            case OwnHomeData:
                // Check for completed an attack
                summarize((Map<String, Object>) message.get("resources"));
                break;

            case EndClientTurn:
                accumulateCost((Object[]) message.get("commands"));
                break;
        }
    }

    /**
     * Sum the troop costs given Place Attacker commands.
     *
     * @param commands an array of EndClientTurn commands (non troop placement commands will be ignored)
     */
    private void accumulateCost(Object[] commands) {
        SessionState state = ProxySession.getSession().getSessionState();
        AttackState attackState = (AttackState) state.getAttribute("attack.state");
        if (attackState != null) {
            for (Object o : commands) {
                Map<String, Object> command = (Map<String, Object>) o;
                switch ((int) command.get("id")) {
                    case 600: // Place attacker
                    case 604: // Cast spell
                        int typeId = (int) command.get("typeId");
                        attackState.accumulateCost(typeId);
                        break;
                }
            }
        }
    }

    /**
     * @param resources resources before attack
     */
    private void setup(Map<String, Object> resources) {
        SessionState state = ProxySession.getSession().getSessionState();
        LootCollection loot = villageAnalyzer.sumStorage(resources);

        Map<Integer, Integer> levelMap = new HashMap<>();

        Object[] troopLevels = (Object[]) resources.get("unitLevels");
        for (Object o : troopLevels) {
            Map<String, Object> troopLevel = (Map<String, Object>) o;
            levelMap.put((Integer) troopLevel.get("type"), (Integer) troopLevel.get("value"));
        }

        Object[] spellLevels = (Object[]) resources.get("spellLevels");
        for (Object o : spellLevels) {
            Map<String, Object> spellLevel = (Map<String, Object>) o;
            levelMap.put((Integer) spellLevel.get("type"), (Integer) spellLevel.get("value"));
        }

        AttackState attackState = new AttackState(loot.getStorageLoot(), levelMap);
        state.setAttribute("attack.state", attackState);
    }

    /**
     * @param resources resources after attack
     */
    private void summarize(Map<String, Object> resources) {
        SessionState state = ProxySession.getSession().getSessionState();
        AttackState attackState = (AttackState) state.getAttribute("attack.state");
        if (attackState != null) {
            Loot loot = villageAnalyzer.sumStorage(resources).getStorageLoot();
            if (!loot.isEmpty()) {
                Loot gross = loot.subtract(attackState.getInitialLoot());
                log.info("Raided: {}", gross);
                Loot spend = attackState.getTotalCost();
                log.info("Spent:  {}", spend);
                Loot net = gross.subtract(spend);
                log.info("Net:    {}", net);
                if (spend.getElixir() != 0) {
                    log.info("Profit: {}", String.format("%.2f%%", net.getElixir() * 100.0 / spend.getElixir()));
                }
            }
            state.setAttribute("attack.state", null);
        }
    }

    /**
     * Remember initial storage levels and accumulate troop costs during an attack.
     */

    public class AttackState {
        private final Map<Integer, Integer> levelMap;
        private Loot initialLoot;
        private Loot totalCost = Loot.ZERO;

        /**
         * @param initialLoot initial storage loot
         * @param levelMap map from unit type id -> unit level in this attack
         */
        public AttackState(Loot initialLoot, Map<Integer, Integer> levelMap) {
            this.initialLoot = initialLoot;
            this.levelMap = levelMap;
        }

        public Loot getInitialLoot() {
            return initialLoot;
        }

        public Loot getTotalCost() {
            return totalCost;
        }

        /**
         * Add the training cost of a unit to the running total.
         *
         * @param typeId the unit type (troop or spell)
         */
        public void accumulateCost(int typeId) {
            int cost = logic.getInt(typeId, "TrainingCost", getLevel(typeId));
            String resourceType = logic.getString(typeId, "TrainingResource");
            int e = 0;
            int de = 0;
            switch (resourceType) {
                case "Elixir":
                    e = cost;
                    break;

                case "DarkElixir":
                    de = cost;
                    break;

                default:
                    log.error("Unknown training resource type {}", resourceType);
                    break;
            }

            totalCost = totalCost.add(new Loot(0, e, de));
        }

        /**
         * Get the level of a troop/spell used in this attack session.
         *
         * @param typeId the type of troop
         * @return the troop level
         */
        private int getLevel(int typeId) {
            Integer level = levelMap.get(typeId);
            if (level == null) {
                log.error("Level not known for {}", typeId);
                return 0;
            }
            return level;
        }
    }
}
