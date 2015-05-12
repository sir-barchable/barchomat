package sir.barchable.clash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.LootCalculator.Loot;
import sir.barchable.clash.model.LootCalculator.LootCollection;
import sir.barchable.clash.model.SessionState;
import sir.barchable.clash.protocol.Message;
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
    public static final String ATTACK_STATE_KEY = "attack.state";

    private Logic logic;
    private VillageAnalyzer villageAnalyzer;

    public AttackAnalyzer(Logic logic) {
        this.logic = logic;
        this.villageAnalyzer = new VillageAnalyzer(logic);
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getType()) {
            case EnemyHomeData:
                // Set up for attack
                setup(message);
                break;

            case OwnHomeData:
                // Check for completed an attack
                summarize(message.getMessage("resources"));
                break;

            case EndClientTurn:
                accumulateCost(message.getArray("commands"));
                break;
        }
    }

    /**
     * Sum the troop costs given Place Attacker commands.
     *
     * @param commands an array of EndClientTurn commands (non troop placement commands will be ignored)
     */
    private void accumulateCost(Message[] commands) {
        SessionState state = ProxySession.getSession().getSessionState();
        AttackState attackState = (AttackState) state.getAttribute(ATTACK_STATE_KEY);
        if (attackState != null) {
            for (Message command : commands) {
                switch (command.getInt("id")) {
                    case 600: // Place attacker
                    case 604: // Cast spell
                        int typeId = (int) command.get("typeId");
                        attackState.accumulateCost(typeId);
                        break;

                    case 700: // Next
                        attackState.nextMatch();
                        break;
                }
            }
        }
    }

    /**
     * @param resources resources before attack
     */
    private void setup(Message resources) {
        SessionState state = ProxySession.getSession().getSessionState();
        AttackState attackState = (AttackState) state.getAttribute(ATTACK_STATE_KEY);
        if (attackState == null) {
            LootCollection loot = villageAnalyzer.sumStorage(resources);

            Map<Integer, Integer> levelMap = new HashMap<>();

            Message[] troopLevels = resources.getArray("unitLevels");
            for (Message troopLevel : troopLevels) {
                levelMap.put(troopLevel.getInt("type"), troopLevel.getInt("value"));
            }

            Message[] spellLevels = resources.getArray("spellLevels");
            for (Message spellLevel : spellLevels) {
                levelMap.put(spellLevel.getInt("type"), spellLevel.getInt("value"));
            }

            int matchCost = logic.getInt("townhall_levels:" + state.getTownHallLevel(), "AttackCost");

            attackState = new AttackState(matchCost, loot.getStorageLoot(), levelMap);
            state.setAttribute(ATTACK_STATE_KEY, attackState);
        }
    }

    /**
     * @param resources resources after attack
     */
    private void summarize(Message resources) {
        SessionState state = ProxySession.getSession().getSessionState();
        AttackState attackState = (AttackState) state.getAttribute(ATTACK_STATE_KEY);
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
            state.setAttribute(ATTACK_STATE_KEY, null);
        }
    }

    /**
     * Remember initial storage levels and accumulate troop costs during an attack.
     */

    public class AttackState {
        private final Map<Integer, Integer> levelMap;
        private final int matchCost;
        private Loot initialLoot;
        private Loot totalCost = Loot.ZERO;
        private int totalMatchCost;

        /**
         * @param matchCost cost of a match
         * @param initialLoot initial storage loot
         * @param levelMap map from unit type id -> unit level in this attack
         */
        public AttackState(int matchCost, Loot initialLoot, Map<Integer, Integer> levelMap) {
            this.matchCost = matchCost;
            this.totalMatchCost = matchCost; // First match is the setup
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

        public void nextMatch() {
            totalMatchCost += matchCost;
        }

        public int getTotalMatchCost() {
            return totalMatchCost;
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
