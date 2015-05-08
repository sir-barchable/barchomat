package sir.barchable.clash.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.ResourceException;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.protocol.*;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.model.SessionState;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static sir.barchable.clash.protocol.Pdu.Type.*;

/**
 * Clash server.
 *
 * @author Sir Barchable
 *         Date: 01/05/15
 */
public class ServerSession {
    private static final Logger log = LoggerFactory.getLogger(ServerSession.class);

    private AtomicBoolean running = new AtomicBoolean(true);
    private Connection clientConnection;
    private MessageFactory messageFactory;

    /**
     * Name of the loadout to use when attacking, or null to use the one from the home data.
     */
    private String loadout;

    /**
     * Attack the war layout?
     */
    private boolean war = true;

    private SessionState sessionState = new SessionState();

    /**
     * File system access.
     */
    private VillageLoader villageLoader;

    /**
     * Loadout management
     */
    private LoadoutManager loadoutManager;

    private boolean dirty;

    private ServerSession(Logic logic, MessageFactory messageFactory, Connection clientConnection, String loadout, File homeFile) throws IOException {
        this.messageFactory = messageFactory;
        this.clientConnection = clientConnection;
        this.villageLoader = new VillageLoader(logic, messageFactory, homeFile, new File("villages"));
        this.loadoutManager = new LoadoutManager(logic, new File("loadouts"));
        if (loadout != null) {
            if (!loadoutManager.contains(loadout)) {
                log.warn("Loadout {} not found", loadout);
            } else {
                this.loadout = loadout;
            }
        }
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    /**
     * Thread local session.
     */
    private static final InheritableThreadLocal<ServerSession> localSession = new InheritableThreadLocal<>();

    /**
     * Serve a clash session. This will block until processing completes, or until the calling thread is interrupted.
     * <p>
     * Normal completion is usually the result of an EOF on the input stream.
     */
    public static ServerSession newSession(Logic logic, MessageFactory messageFactory, Connection clientConnection, String loadout, File homeFile) throws IOException {
        ServerSession session = new ServerSession(logic, messageFactory, clientConnection, loadout, homeFile);
        localSession.set(session);
        try {

            //
            // We run the uninterruptable IO in a separate thread to maintain an interruptable controlling thread from
            // which can stop processing by closing the input stream.
            //

            Thread t = new Thread(session::run, clientConnection.getName() + " server");
            t.start();
            t.join();

        } catch (InterruptedException e) {
            session.shutdown();
        } finally {
            localSession.set(null);
        }
        return session;
    }

    private void save() {
        villageLoader.save();
    }

    /**
     * Process the key exchange then loop to process PDUs from the client.
     */
    private void run() {
        try {

            //
            // First packet is the login from the client.
            // It contains the seed for the key generator.
            //

            Pdu loginPdu = clientConnection.getIn().read();
            Message loginMessage = messageFactory.fromPdu(loginPdu);
            if (loginMessage.getType() != Login) {
                throw new IllegalStateException("Expected Login");
            }
            Long userId = loginMessage.getLong("userId");
            if (userId == null) {
                throw new PduException("No user id in login");
            }
            sessionState.setUserId(userId);

            Object clientSeed = loginMessage.get("clientSeed");
            if (clientSeed == null || !(clientSeed instanceof Integer)) {
                throw new PduException("Expected client seed in login message");
            }
            Clash7Random prng = new Clash7Random((Integer) clientSeed);

            //
            // Generate a nonce and pass it back to the client
            //

            Message encryptionMessage = messageFactory.newMessage(Encryption);

            byte[] nonce = new byte[24];
            ThreadLocalRandom.current().nextBytes(nonce); // generate a new key
            encryptionMessage.set("serverRandom", nonce);
            encryptionMessage.set("version", 1);
            clientConnection.getOut().write(messageFactory.toPdu(encryptionMessage));

            //
            // Re-key the streams
            //

            clientConnection.setKey(prng.scramble(nonce));

            //
            // Then tell the client that all is well
            //

            Message loginOkMessage = messageFactory.newMessage(LoginOk);

            loginOkMessage.set("userId", userId);
            loginOkMessage.set("homeId", userId);
            loginOkMessage.set("userToken", loginMessage.get("userToken"));
            loginOkMessage.set("majorVersion", loginMessage.get("majorVersion"));
            loginOkMessage.set("minorVersion", loginMessage.get("minorVersion"));
            loginOkMessage.set("environment", "prod");
            loginOkMessage.set("loginCount", 1);
            loginOkMessage.set("timeOnline", 1);
            loginOkMessage.set("lastLoginDate", "" + System.currentTimeMillis() / 1000);
            loginOkMessage.set("country", "US");

            clientConnection.getOut().write(messageFactory.toPdu(loginOkMessage));

            //
            // Send the home village
            //

            clientConnection.getOut().write(messageFactory.toPdu(loadHome()));

            //
            // The request loop handles further PDUs
            //

            processRequests(clientConnection);

        } catch (PduException | IOException e) {
            log.error("Key exchange did not complete: " + e, e);
        }
    }

    private void processRequests(Connection connection) {
        try {
            while (running.get()) {

                //
                // Read a request PDU
                //

                Pdu pdu = connection.getIn().read();
                log.debug("Pdu from client: {}", pdu.getType());

                Message request;
                try {
                    request = messageFactory.fromPdu(pdu);
                } catch (RuntimeException e) {
                    // Probably no type definition for the PDU
                    log.debug("Can't respond to {}: {}", pdu.getType(), e.getMessage());
                    continue;
                }

                //
                // Create a response
                //

                Message response = null;

                switch (pdu.getType()) {
                    case EndClientTurn:
                        response = endTurn(request);
                        break;

                    case AttackResult:
                        response = loadHome();
                        break;

                    case KeepAlive:
                        response = messageFactory.newMessage(ServerKeepAlive);
                        break;

                    default:
                        log.debug("Not handling {} from {}", pdu.getType(), connection.getName());
                }

                //
                // Return the response to the client
                //

                if (response != null) {
                    connection.getOut().write(messageFactory.toPdu(response));
                }
            }

            log.info("{} done", connection.getName());
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            log.info(
                "{} terminating due to exception {}",
                connection.getName(),
                e.getMessage() == null ? e.toString() : e.getMessage()
            );
        }
    }

    private Message endTurn(Message message) throws IOException {
        Message response = null;
        Object[] commands = (Object[]) message.get("commands");
        if (commands != null) {
            commandLoop: for (int i = 0; i < commands.length; i++) {
                Map<String, Object> command = (Map<String, Object>) commands[i];
                Integer id = (Integer) command.get("id");
                if (id != null) {
                    switch (id) {
                        case 700:
                            response = loadEnemy();
                            break commandLoop;

                        case 603:
                            response = loadHome();
                            break commandLoop;

                        case 501:   // Move building
                            moveBuilding((int) command.get("x"), (int) command.get("y"), (int) command.get("buildingId"));
                            break;

                        default:
                            // We're lost; give up
                            log.debug("Not processing command {} from client", id);
                            break commandLoop;
                    }
                }
            }
        }

        if (dirty) {
            save();
        }

        return response;
    }

    private void moveBuilding(int x, int y, int buildingId) {
        log.debug("Moving {} to {}, {}", buildingId, x, y);
        int offset = buildingId % 1000;
        int type = buildingId / 1000000;

        Village village = villageLoader.getHomeVillage();
        Village.Building building = null;

        try {
            switch (type) {
                case 500:
                    building = village.buildings[offset];
                    break;

                case 504:
                    building = village.traps[offset];
                    break;

                case 506:
                    building = village.decos[offset];
                    break;

                default:
                    log.debug("You moved what?");
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Couldn't find building {}", buildingId);
        }

        if (building != null) {
            building.x = x;
            building.y = y;
            dirty = true;
        }
    }

    private Message loadHome() throws IOException {
        Message village = villageLoader.getOwnHomeData();
        if (village == null) {
            throw new ResourceException("No home village. Have you captured some data with the proxy?");
        }
        village.set("timeStamp", (int) (System.currentTimeMillis() / 1000));
        // Set remaining shield to to avoid annoying attack confirmation dialog
        village.set("remainingShield", 0);
        return village;
    }

    private void applyLoadout(Message village) throws IOException {
        if (loadout != null) {
            loadoutManager.applyLoadOut(village, loadout);
        }
    }

    private int nextVillage;

    private Message loadEnemy() throws IOException {
        Message village = villageLoader.loadEnemyVillage(nextVillage++, war);
        if (village == null) {
            throw new ResourceException("No home village. Have you captured some data with the proxy?");
        }
        village.set("timeStamp", (int) (System.currentTimeMillis() / 1000));
        applyLoadout(village);

        return village;
    }

    /**
     * A hint that processing should stop. Just sets a flag and waits for the processing threads to notice. If you
     * really want processing to stop in a hurry close the input streams.
     */
    public void shutdown() {
        running.set(false);
    }
}
