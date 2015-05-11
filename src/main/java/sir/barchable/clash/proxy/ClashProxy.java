package sir.barchable.clash.proxy;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.AttackAnalyzer;
import sir.barchable.clash.VillageAnalyzer;
import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.LogicParser;
import sir.barchable.clash.protocol.*;
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

import static sir.barchable.clash.protocol.Pdu.Type.EndClientTurn;
import static sir.barchable.clash.protocol.Pdu.Type.WarHomeData;

/**
 * Clash proxy.
 */
public class ClashProxy {
    public static final int CLASH_PORT = 9339;

    private static final Logger log = LoggerFactory.getLogger(ClashProxy.class);

    @Parameter(names = {"-d", "--definition-dir"}, description = "Directory to load the protocol definition from")
    private File resourceDir;

    @Parameter(names = {"-l", "--logic"}, description = "Directory/file to load the game logic from")
    private File logicFile;

    @Parameter(names = {"-w", "--working-dir"}, description = "Directory write output to")
    private File workingDir = new File(".");

    @Parameter(names = {"-s", "--save"}, description = "Save messages to the 'villages' directory")
    private boolean save;

    @Parameter(names = {"-n", "--name-server"}, description = "Name server to read up-stream server address from")
    private String nameServer = "8.8.8.8";

    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Listen on {@link #CLASH_PORT}.
     */
    private ServerSocket listener;

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
     *
     * @see #nameServer
     */
    private Dns dns;

    private MessageFactory messageFactory;

    /**
     * Entry point.
     *
     * @param args see usage
     */
    public static void main(String[] args) {
        ClashProxy proxy = new ClashProxy();
        JCommander commander = new JCommander(proxy);
        try {
            commander.parse(args);
            proxy.run();
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

        //
        // Look up the server using an external DNS because the internal one is probably being used to redirect
        // the client to this proxy. The default, 8.8.8.8, is one of Google's public DNS servers.
        //

        this.dns = new Dns(nameServer);

        this.listener = new ServerSocket(CLASH_PORT);

        if (!workingDir.exists() || !workingDir.isDirectory()) {
            throw new ParameterException(workingDir.toString());
        }

        //
        // Read the protocol definition
        //

        TypeFactory typeFactory;
        if (resourceDir != null) {
            typeFactory = new TypeFactory(new ProtocolTool(resourceDir).read());
        } else {
            typeFactory = new TypeFactory();
        }
        messageFactory = new MessageFactory(typeFactory);

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
        Logic logic = LogicParser.loadLogic(logicFile);

        //
        // This filter prints stuff
        //

        MessageLogger logger = new MessageLogger();
        filterChain = filterChain.addAfter(new MessageTapFilter(
            messageFactory,
            new VillageAnalyzer(logic),
            new AttackAnalyzer(logic)
//            logger.tapFor(EndClientTurn),
//            logger.tapFor(WarHomeData, "warVillage")
        ));

        //
        // This filter dumps village messages to the working directory
        //

        if (save) {
            File villageDir = new File(workingDir, "villages");
            if (villageDir.mkdir()) {
                log.info("Created save directory for villages: {}", villageDir);
            }
            filterChain = filterChain.addAfter(new MessageSaver(messageFactory, villageDir));
        }

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
                ProxySession session = ProxySession.newSession(messageFactory, clientConnection, serverConnection, filterChain);
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
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
