package sir.barchable.clash.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.ResourceException;
import sir.barchable.clash.model.LoadOut;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.protocol.*;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.model.SessionState;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static sir.barchable.clash.protocol.Pdu.Type.Encryption;
import static sir.barchable.clash.protocol.Pdu.Type.LoginOk;
import static sir.barchable.clash.protocol.Pdu.Type.ServerKeepAlive;

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
    private SessionState sessionState = new SessionState();

    /**
     * File system access.
     */
    private VillageLoader villageLoader;

    private ServerSession(Logic logic, MessageFactory messageFactory, Connection clientConnection) throws IOException {
        this.messageFactory = messageFactory;
        this.clientConnection = clientConnection;
        this.villageLoader = new VillageLoader(logic, messageFactory, new File("villages"));
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
    public static ServerSession newSession(Logic logic, MessageFactory messageFactory, Connection clientConnection) throws IOException {
        ServerSession session = new ServerSession(logic, messageFactory, clientConnection);
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

    /**
     * Process the key exchange then loop to process PDUs from the client.
     */
    private void run() {
        try {

            //
            // First packet is the login from the client.
            // It contains the seed for the key generator.
            //

            Pdu loginPdu = clientConnection.getIn().readPdu();
            Message loginMessage = messageFactory.fromPdu(loginPdu);
            sessionState.setUserId((Long) loginMessage.get("userId"));
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
            clientConnection.getOut().writePdu(messageFactory.toPdu(encryptionMessage));

            //
            // Re-key the streams
            //

            clientConnection.setKey(prng.scramble(nonce));

            //
            // Then tell the client that all is well
            //

            Message loginOkMessage = messageFactory.newMessage(LoginOk);

            loginOkMessage.set("userId", loginMessage.get("userId"));
            loginOkMessage.set("homeId", loginMessage.get("userId"));
            loginOkMessage.set("userToken", loginMessage.get("userToken"));
            loginOkMessage.set("majorVersion", loginMessage.get("majorVersion"));
            loginOkMessage.set("minorVersion", loginMessage.get("minorVersion"));
            loginOkMessage.set("environment", "prod");
            loginOkMessage.set("loginCount", 1);
            loginOkMessage.set("timeOnline", 1);
            loginOkMessage.set("lastLoginDate", "" + System.currentTimeMillis() / 1000);
            loginOkMessage.set("country", "US");

            clientConnection.getOut().writePdu(messageFactory.toPdu(loginOkMessage));

            //
            // Send the home village
            //

            clientConnection.getOut().writePdu(messageFactory.toPdu(loadHome()));

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

                Pdu pdu = connection.getIn().readPdu();
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
                    connection.getOut().writePdu(messageFactory.toPdu(response));
                }
            }

            log.info("{} done", connection.getName());
        } catch (RuntimeException | IOException e) {
            log.info(
                "{} terminating due to exception {}",
                connection.getName(),
                e.getMessage() == null ? e.toString() : e.getMessage()
            );
        }
    }

    private Message endTurn(Message message) throws IOException {
        Object[] commands = (Object[]) message.get("commands");
        if (commands != null) {
            for (int i = 0; i < commands.length; i++) {
                Map<String, Object> command = (Map<String, Object>) commands[i];
                Integer id = (Integer) command.get("id");
                if (id != null) {
                    switch (id) {
                        case 700:
                            return loadEnemy();

                        case 603:
                            return loadHome();

                        default:
                            log.debug("Not processing command {} from client", id);
                    }
                }
            }
        }
        return null;
    }

    private Message loadHome() throws IOException {
        Message village = villageLoader.loadHomeVillage();
        if (village == null) {
            throw new ResourceException("No home village. Have you captured some data with the proxy?");
        }
        village.set("timeStamp", (int) (System.currentTimeMillis() / 1000));
        applyLoadout(village);

        return village;
    }

    private void applyLoadout(Message village) throws IOException {
        File loadOutFile = new File("loadout.json");
        if (loadOutFile.exists()) {
            LoadOut loadOut = villageLoader.loadLoadOut(loadOutFile);
            villageLoader.applyLoadOut(village, loadOut);
        }
    }

    private int nextVillage;

    private Message loadEnemy() throws IOException {
        Message village = villageLoader.loadEnemyVillage(nextVillage++);
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
