package sir.barchable;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.VillageAnalyzer;
import sir.barchable.clash.model.LogicParser;
import sir.barchable.clash.protocol.*;
import sir.barchable.clash.proxy.MessageLogger;
import sir.barchable.clash.proxy.MessageTapFilter;
import sir.barchable.clash.proxy.ProxySession;
import sir.barchable.util.Hex;
import sir.barchable.util.Json;

import java.io.*;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static sir.barchable.util.BitBucket.NOWHERE;

/**
 * Decrypt a session
 *
 * @author Sir Barchable
 */
public class Dump {
    private static final Logger log = LoggerFactory.getLogger(Dump.class);

    @Parameter(names = {"-d", "--definition-dir"}, description = "Directory to load the protocol definition from")
    private File resourceDir;

    @Parameter(names = {"-l", "--logic"}, description = "Directory/file to load the game logic from")
    private File logicFile;

    @Parameter(names = {"-w", "--working-dir"}, description = "Directory to read streams from")
    private File workingDir = new File(".");

    @Parameter(names = {"-h", "--hex"}, description = "Dump messages as hex")
    private boolean dumpHex;

    @Parameter(names = {"-j", "--json"}, description = "Dump messages as json")
    private boolean dumpJson;

    public static void main(String[] args) throws IOException {
        Dump main = new Dump();
        JCommander commander = new JCommander(main);
        try {
            commander.parse(args);
            main.run();
        } catch (ParameterException e) {
            commander.usage();
        } catch (Exception e) {
            log.error("", e);
            System.err.println("Oops: " + e.getMessage());
        }
    }

    private TypeFactory typeFactory;
    private MessageReader messageReader;

    private void run() throws IOException, InterruptedException {
        if (!workingDir.exists()) {
            throw new FileNotFoundException(workingDir.toString());
        }

        if (resourceDir != null) {
            if (!resourceDir.exists()) {
                throw new FileNotFoundException(resourceDir.toString());
            }
            typeFactory = new TypeFactory(new ProtocolTool(resourceDir).read());
        } else {
            typeFactory = new TypeFactory();
        }

        if (logicFile == null) {
            File[] apks = new File(".").listFiles((dir, name) -> name.endsWith(".apk"));
            if (apks.length != 1) {
                throw new FileNotFoundException("Logic file not specified");
            } else {
                logicFile = apks[0];
            }
        }

        messageReader = new MessageReader(typeFactory);

        File clientDumpFile = new File(workingDir, "client.txt");
        File serverDumpFile = new File(workingDir, "server.txt");
        try (
            // Client output stream
            OutputStreamWriter clientOut = new OutputStreamWriter(new FileOutputStream(clientDumpFile), UTF_8);
            // Server output stream
            OutputStreamWriter serverOut = new OutputStreamWriter(new FileOutputStream(serverDumpFile), UTF_8)
        ) {
            dumpSession(clientOut, serverOut);
        }
    }

    /**
     * Decrypt and dump a session. Expects two files in the working directory, <i>client.stream</i> and
     * <i>server.stream</i>, containing raw tcp stream captures from a clash session.
     * <p>
     * Capture: <code>tcpflow port 9339</code>
     * @param clientOut where to write the decoded client stream
     * @param serverOut where to write the decoded server stream
     */
    public void dumpSession(Writer clientOut, Writer serverOut) throws IOException, InterruptedException {
        Dumper clientDumper = new Dumper(Pdu.Origin.Client, clientOut);
        Dumper serverDumper = new Dumper(Pdu.Origin.Server, serverOut);
        File clientFile = new File(workingDir, "client.stream");
        File serverFile = new File(workingDir, "server.stream");
        MessageTapFilter tapFilter = new MessageTapFilter(
            messageReader,
            new VillageAnalyzer(LogicParser.loadLogic(logicFile)),
            new MessageLogger(new OutputStreamWriter(System.out)).tapFor(Pdu.Type.WarHomeData, "warVillage")
        );
        try (
            // Client connection
            Connection clientConnection = new Connection("Client", new FileInputStream(clientFile), NOWHERE);
            // Server connection
            Connection serverConnection = new Connection("Server", new FileInputStream(serverFile), NOWHERE)
        ) {
            ProxySession session = ProxySession.newSession(clientConnection, serverConnection, clientDumper::dump, serverDumper::dump, tapFilter);
            VillageAnalyzer.logSession(session);
        }
    }

    /**
     * Formats and writes PDUs to an output stream.
     */
    class Dumper {
        private final Pdu.Origin origin;
        private Writer out;

        /**
         * Construct a dumper.
         *
         * @param origin the type of Pdu to dump
         * @param out the stream to write to.
         */
        public Dumper(Pdu.Origin origin, Writer out) {
            this.origin = origin;
            this.out = out;
        }

        synchronized public Pdu dump(Pdu pdu) throws IOException {
            if (pdu.getOrigin() == origin) {
                if (dumpHex) {
                    dumpHex(pdu);
                }
                if (dumpJson) {
                    dumpJson(pdu);
                }
                out.flush();
            }
            return pdu;
        }

        void dumpJson(Pdu pdu) throws IOException {
            Map<String, Object> message = messageReader.readMessage(pdu);
            if (message != null) {
                String name = typeFactory.getStructNameForId(pdu.getId()).get();
                out.write('"' + name + "\": ");
                Json.write(message, out);
                out.write('\n');
            }
        }

        private void dumpHex(Pdu pdu) throws IOException {
            out.write(pdu.toString() + "\n");
            Hex.dump(pdu.getPayload(), out);
        }
    }
}
