package sir.barchable.clash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.ClashServices;
import sir.barchable.clash.Main;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.ClashProxy;
import sir.barchable.clash.server.ServerSession;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class ClashServer {
    public static final int CLASH_PORT = 9339;

    private static final Logger log = LoggerFactory.getLogger(ClashProxy.class);

    /**
     * Common services.
     */
    private final ClashServices services;

    /**
     * Command line config.
     */
    private final Main.ServerCommand command;

    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Source of threads to accept connections.
     */
    private ExecutorService executor = Executors.newCachedThreadPool();

    public ClashServer(ClashServices services, Main.ServerCommand command) {
        this.services = services;
        this.command = command;
    }

    public void run() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread((this::shutdownNow)));

        try (ServerSocket listener = new ServerSocket(CLASH_PORT)) {
            log.info("Listening on {}", CLASH_PORT);
            while (running.get()) {
                Socket socket = listener.accept();
                if (running.get()) {
                    executor.execute(() -> accept(socket));
                } else {
                    socket.close();
                }
            }
        } catch (IOException e) {
            // exit
        }
    }

    /**
     * Accept a connection from a client. Will block until processing completes (typically because the client closes
     * its connection to us).
     * <p>
     * The socket and any associated streams will be closed before this call completes.
     */
    private void accept(Socket socket) {
        log.info("Client connected from {}", socket.getInetAddress());
        try {
            try (
                Connection clientConnection = new Connection(socket);
            ) {
                ServerSession session = ServerSession.newSession(services, clientConnection, command);
                log.info("Client {} disconnected", socket);
            }
        } catch (IOException e) {
            log.info("Could not accept connection from {}: {}", socket.getInetAddress(), e.toString());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void shutdownNow() {
        if (running.compareAndSet(true, false)) {
            log.info("Exiting...");

            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
