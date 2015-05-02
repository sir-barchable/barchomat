package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static sir.barchable.clash.protocol.Pdu.ID.Encryption;

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

    private ServerSession(MessageFactory messageFactory, Connection clientConnection) {
        this.messageFactory = messageFactory;
        this.clientConnection = clientConnection;
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
    public static ServerSession newSession(MessageFactory messageFactory, Connection clientConnection) {
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
            // First capture the login message from the client
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
            // And send the encryption key back
            //

            Message encryptionMessage = messageFactory.newMessage(Encryption);

            byte[] nonce = new byte[24];
            ThreadLocalRandom.current().nextBytes(nonce); // generate a new key
            encryptionMessage.set("serverRandom", nonce);
            encryptionMessage.set("version", 1);
            clientConnection.getOut().writePdu(messageFactory.toPud(encryptionMessage));

            clientConnection.setKey(prng.scramble(nonce));
        } catch (PduException | IOException e) {
            log.error("Key exchange did not complete: " + e);
        }
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
