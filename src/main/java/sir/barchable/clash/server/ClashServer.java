package sir.barchable.clash.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.LogicParser;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.protocol.ProtocolTool;
import sir.barchable.clash.protocol.TypeFactory;
import sir.barchable.clash.proxy.ClashProxy;
import sir.barchable.clash.proxy.ProxySession;

import java.io.File;
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

    @Parameter(names = {"-d", "--definition-dir"}, description = "Directory to load the protocol definition from")
    private File resourceDir;

    @Parameter(names = {"-l", "--logic"}, description = "Directory/file to load the game logic from")
    private File logicFile;

    @Parameter(names = {"-w", "--working-dir"}, description = "Directory write output to")
    private File workingDir = new File(".");

    @Parameter(names = {"--loadout"}, description = "Name of loadout to apply")
    private String loadout;

    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Listen on {@link #CLASH_PORT}.
     */
    private ServerSocket listener;

    /**
     * Source of threads to accept connections.
     */
    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Pdu serialization.
     */
    private MessageFactory messageFactory;

    /**
     * Game config.
     */
    private Logic logic;

    /**
     * Entry point.
     *
     * @param args see usage
     */
    public static void main(String[] args) {
        ClashServer server = new ClashServer();
        JCommander commander = new JCommander(server);
        try {
            commander.parse(args);
            server.run();
        } catch (ParameterException e) {
            commander.usage();
        } catch (Exception e) {
            log.error("Oops: ", e);
        }
    }

    /**
     * Process command line parameters.
     */
    private void init() throws IOException {

        this.listener = new ServerSocket(CLASH_PORT);

        if (!workingDir.exists() || !workingDir.isDirectory()) {
            throw new ParameterException(workingDir.toString());
        }

        //
        // Read the protocol definition
        //

        TypeFactory typeFactory;
        if (resourceDir != null) {
            typeFactory = new TypeFactory(ProtocolTool.read(resourceDir));
        } else {
            typeFactory = new TypeFactory();
        }
        this.messageFactory = new MessageFactory(typeFactory);

        //
        // Load the logic files
        //

        if (logicFile == null) {
            File[] apks = workingDir.listFiles((dir, name) -> name.endsWith(".apk"));
            if (apks.length != 1) {
                throw new ParameterException("Logic file not specified");
            } else {
                logicFile = apks[0];
            }
        }
        this.logic = LogicParser.loadLogic(logicFile);

        // Clean-up thread
        Runtime.getRuntime().addShutdownHook(new Thread((this::shutdownNow)));
    }

    private void run() throws IOException {
        init();

        try {
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
                ServerSession session = ServerSession.newSession(logic, messageFactory, clientConnection, loadout);
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

            //
            // Close the server socket
            //

            try {
                listener.close();
            } catch (IOException e) {
                // Meh
            }

            executor.shutdownNow();
            try {
                executor.awaitTermination(ProxySession.SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
