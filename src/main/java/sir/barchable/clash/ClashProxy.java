package sir.barchable.clash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.proxy.MessageSaver;
import sir.barchable.clash.proxy.MessageTapFilter;
import sir.barchable.clash.proxy.PduFilterChain;
import sir.barchable.clash.proxy.ProxySession;
import sir.barchable.util.Dns;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Clash proxy.
 */
public class ClashProxy {
    public static final int CLASH_PORT = 9339;

    private static final Logger log = LoggerFactory.getLogger(ClashProxy.class);

    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Common services.
     */
    ClashServices services;

    /**
     * Filter to run PDUs through.
     */
    private PduFilterChain filterChain = new PduFilterChain();

    /**
     * Source of threads to accept connections.
     */
    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Source for the address of the real server.
     */
    private Dns dns;

    public ClashProxy(ClashServices services, Main.ProxyCommand command) throws IOException {

        this.services = services;

        //
        // Look up the server using an external DNS because the internal one is probably being used to redirect
        // the client to this proxy. The default, 8.8.8.8, is one of Google's public DNS servers.
        //

        this.dns = new Dns(command.getNameServer());

        //
        // This filter prints stuff
        //

        filterChain = filterChain.addAfter(new MessageTapFilter(
            services.getMessageFactory(),
            new VillageAnalyzer(services.getLogic()),
            new AttackAnalyzer(services.getLogic())
//            logger.tapFor(EndClientTurn),
//            logger.tapFor(WarHomeData, "warVillage")
        ));

        //
        // This filter dumps village messages to the working directory
        //

        if (command.getSave()) {
            File villageDir = new File(services.getWorkingDir(), "villages");
            if (villageDir.mkdir()) {
                log.info("Created save directory for villages: {}", villageDir);
            }
            filterChain = filterChain.addAfter(
                new MessageSaver(services.getMessageFactory(), villageDir)
            );
        }
    }

    public void run() throws IOException {
        // Clean-up thread
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
            log.debug("Terminated with exception: {}", e.toString());
        }
    }

    /**
     * Accept a connection from a client and proxy it to the server. Will block until processing completes (typically
     * because the client closes its connection to us).
     * <p>
     * The socket and any associated streams will be closed before this call completes.
     */
    private void accept(Socket socket) {
        log.info("Client connected from {}", socket.getInetAddress());
        try {
            InetAddress serverAddress = dns.getAddress("gamea.clashofclans.com");
            try (
                Connection clientConnection = new Connection(socket);
                Connection serverConnection = new Connection(new Socket(serverAddress, CLASH_PORT))
            ) {
                ProxySession session = ProxySession.newSession(
                    services.getMessageFactory(), clientConnection, serverConnection, filterChain
                );
                log.info("Client {} disconnected", socket);
                VillageAnalyzer.logSession(session);
            }
        } catch (IOException e) {
            log.info("Could not proxy connection from {}: {}", socket.getInetAddress(), e.toString());
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
