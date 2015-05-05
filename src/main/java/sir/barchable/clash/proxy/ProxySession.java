package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.SessionState;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.protocol.PduException;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to push data through the pipes until EOF.
 *
 * @author Sir Barchable
 *         Date: 15/04/15
 */
public class ProxySession {
    private static final Logger log = LoggerFactory.getLogger(ProxySession.class);

    /**
     * Shutdown timeout in milliseconds
     */
    public static final int SHUTDOWN_TIMEOUT = 2500;

    private AtomicBoolean running = new AtomicBoolean(true);

    private PduFilterChain filterChain;
    private Connection clientConnection;
    private Connection serverConnection;

    private SessionState sessionState = new SessionState();

    /**
     * When the pipe threads finish they wait here.
     */
    private CountDownLatch latch = new CountDownLatch(2);

    private ProxySession(Connection clientConnection, Connection serverConnection, PduFilter... filters) {
        this.clientConnection = clientConnection;
        this.serverConnection = serverConnection;
        this.filterChain = new PduFilterChain(filters);
    }

    /**
     * Get the session that your thread is participating in.
     *
     * @return your session, or null
     */
    public static ProxySession getSession() {
        return localSession.get();
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    /**
     * Thread local session.
     */
    private static final InheritableThreadLocal<ProxySession> localSession = new InheritableThreadLocal<>();

    /**
     * Proxy a connection from a client to a clash server. This will block until processing completes, or until the
     * calling thread is interrupted.
     * <p>
     * Normal completion is usually the result of an EOF on one of the input streams.
     */
    public static ProxySession newSession(Connection clientConnection, Connection serverConnection, PduFilter... filters) {
        ProxySession session = new ProxySession(clientConnection, serverConnection, filters);
        localSession.set(session);
        try {
            session.start();
            session.await();
        } catch (InterruptedException e) {
            session.shutdown();
        } finally {
            localSession.set(null);
        }
        return session;
    }

    private void start() {
        // A pipe for messages from client -> server
        Pipe clientPipe = new Pipe(clientConnection.getName(), clientConnection.getIn(), serverConnection.getOut());
        // A pipe for messages from server -> client
        Pipe serverPipe = new Pipe(serverConnection.getName(), serverConnection.getIn(), clientConnection.getOut());

        KeyFilter keyListener = new KeyFilter();
        PduFilter loginFilter = filterChain.addAfter(keyListener);

        try {
            // First capture the login message from the client
            clientPipe.filterThrough(loginFilter);
            // and the the key from the server
            serverPipe.filterThrough(loginFilter);

            byte[] key = keyListener.getKey();

            if (key == null) {
                log.error("Key exchange did not complete");
            } else {
                // Re-key the streams
                clientConnection.setKey(key);
                serverConnection.setKey(key);
                // Proxy messages from client -> server
                runPipe(clientPipe);
                // Proxy messages from server -> client
                runPipe(serverPipe);
            }
        } catch (PduException | IOException e) {
            log.error("Key exchange did not complete: " + e);
        }
    }

    /**
     * This is used by {@link #newSession(Connection, Connection, PduFilter...)} to wait for the pipe threads to
     * complete.
     */
    void await() throws InterruptedException {
        latch.await();
    }

    private void runPipe(Pipe pipe) {
        Thread t = new Thread(() -> {
            try {
                while (running.get()) {
                    pipe.filterThrough(filterChain);
                }
            } catch (EOFException e) {
                log.debug("{} at EOF", pipe.getName());
            } catch (IOException e) {
                log.debug("{} IOException", pipe.getName());
            } catch (RuntimeException e) {
                // It broke unexpectedly
                log.debug("{} closed with exception", pipe.getName(), e);
            }
            latch.countDown();
        }, "Pipe thread for " + pipe.getName());
        t.setDaemon(true);
        t.start();
    }

    /**
     * A hint that processing should stop. Just sets a flag and waits for the processing threads to notice. If you
     * really want processing to stop in a hurry close the input streams.
     */
    public void shutdown() {
        running.set(false);
    }
}
