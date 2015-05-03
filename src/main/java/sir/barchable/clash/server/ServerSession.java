package sir.barchable.clash.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.*;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.model.SessionData;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static sir.barchable.clash.protocol.Pdu.Type.Encryption;
import static sir.barchable.clash.protocol.Pdu.Type.LoginOk;
import static sir.barchable.clash.protocol.Pdu.Type.ServerKeepAlive;

/**
 * @author Sir Barchable
 *         Date: 01/05/15
 */
public class ServerSession {
    private static final Logger log = LoggerFactory.getLogger(ServerSession.class);

    private AtomicBoolean running = new AtomicBoolean(true);
    private Connection clientConnection;
    private CountDownLatch latch = new CountDownLatch(1);
    private MessageFactory messageFactory;
    private SessionData sessionData = new SessionData();

    /**
     * File system access.
     */
    private VillageLoader villageLoader;


    private ServerSession(MessageFactory messageFactory, Connection clientConnection) throws IOException {
        this.messageFactory = messageFactory;
        this.clientConnection = clientConnection;
        this.villageLoader = new VillageLoader(messageFactory, new File("villages"));
    }

    /**
     * Get the session that your thread is participating in.
     *
     * @return your session, or null
     */
    public static ServerSession getSession() {
        return session.get();
    }

    /**
     * Thread local session.
     */
    private static final InheritableThreadLocal<ServerSession> session = new InheritableThreadLocal<>();

    /**
     * Serve a clash session. This will block until processing completes, or until the calling thread is interrupted.
     * <p>
     * Normal completion is usually the result of an EOF on the input stream.
     */
    public static ServerSession newSession(MessageFactory messageFactory, Connection clientConnection) throws IOException {
        ServerSession session = new ServerSession(messageFactory, clientConnection);
        try {
            ServerSession.session.set(session);
            session.start();
            session.await();
        } catch (InterruptedException e) {
            session.shutdown();
        } finally {
            ServerSession.session.set(null);
        }
        return session;
    }

    private void start() {
        try {

            //
            // First packet is the login from the client.
            // It contains the seed for the key generator.
            //

            Pdu loginPdu = clientConnection.getIn().readPdu();
            Message loginMessage = messageFactory.fromPdu(loginPdu);
            sessionData.setUserId((Long) loginMessage.get("userId"));
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

            Message homeVillage = villageLoader.loadHomeVillage();
            clientConnection.getOut().writePdu(messageFactory.toPdu(homeVillage));

            //
            // Hand off to the main loop to start exchanging PDUs.
            //

            run(clientConnection);

        } catch (PduException | IOException e) {
            log.error("Key exchange did not complete: " + e, e);
        }
    }

    private void run(Connection connection) {
        try {
            while (running.get()) {
                Pdu pdu = connection.getIn().readPdu();
                log.debug("Pdu from client: {}", pdu.getType());

                Message message = null;
                try {
                    message = messageFactory.fromPdu(pdu);
                } catch (RuntimeException e) {
                    // Probably no type definition for the PDU
                    log.debug("Can't respond to {}: {}", pdu.getType(), e.getMessage());
                    continue;
                }

                Message response = null;
                switch (pdu.getType()) {
                    case EndClientTurn:
                        response = endTurn(message);
                        break;

                    case KeepAlive:
                        response = messageFactory.newMessage(ServerKeepAlive);
                        break;

                    default:
                        log.debug("{} from {}", pdu.getType(), connection.getName());
                }
                if (response != null) {
                    connection.getOut().writePdu(messageFactory.toPdu(response));
                }
            }
        } catch (IOException e) {
            log.info("{} done", connection);
        }
    }

    private Message endTurn(Message message) {
        return null;
    }

    /**
     * This is used by {@link #newSession} to wait for the session to complete.
     */
    void await() throws InterruptedException {
        latch.await();
    }

    /**
     * Thread local session data.
     *
     * @see ServerSession#getSessionData()
     */
    public SessionData getSessionData() {
        return sessionData;
    }

    /**
     * A hint that processing should stop. Just sets a flag and waits for the processing threads to notice. If you
     * really want processing to stop in a hurry close the input streams.
     */
    public void shutdown() {
        running.set(false);
    }
}
