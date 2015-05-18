package sir.barchable.clash;

import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.Pdu;
import sir.barchable.clash.protocol.PduException;
import sir.barchable.clash.proxy.MessageLogger;
import sir.barchable.clash.proxy.MessageTapFilter;
import sir.barchable.clash.proxy.ProxySession;
import sir.barchable.util.Hex;
import sir.barchable.util.Json;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static sir.barchable.util.BitBucket.NOWHERE;

/**
 * Decrypt a session
 *
 * @author Sir Barchable
 */
public class Decode {
    private ClashServices services;
    private Main.DecodeCommand command;
    private final File workingDir;

    public Decode(ClashServices services, Main.DecodeCommand command) {
        this.services = services;
        this.command = command;
        this.workingDir = services.getWorkingDir();
    }

    public void run() throws IOException, InterruptedException {
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
            services.getMessageFactory(),
            new VillageAnalyzer(services.getLogic()),
            new MessageLogger(new OutputStreamWriter(System.out)).tapFor(Pdu.Type.WarHomeData, "warVillage")
        );
        try (
            // Client connection
            Connection clientConnection = new Connection("Client", new FileInputStream(clientFile), NOWHERE);
            // Server connection
            Connection serverConnection = new Connection("Server", new FileInputStream(serverFile), NOWHERE)
        ) {
            ProxySession session = ProxySession.newSession(services.getMessageFactory(), clientConnection, serverConnection, clientDumper::dump, serverDumper::dump, tapFilter);
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
                if (command.getDumpHex()) {
                    dumpHex(pdu);
                }
                if (command.getDumpJson()) {
                    dumpJson(pdu);
                }
                out.flush();
            }
            return pdu;
        }

        void dumpJson(Pdu pdu) throws IOException {
            try {
                Message message = services.getMessageFactory().fromPdu(pdu);
                if (message != null) {
                    out.write('"' + pdu.getType().name() + "\": ");
                    Json.writePretty(message.getFields(), out);
                    out.write('\n');
                }
            } catch (PduException e) {
                // ignore
            }
        }

        private void dumpHex(Pdu pdu) throws IOException {
            out.write(pdu.toString() + "\n");
            Hex.dump(pdu.getPayload(), out);
        }
    }
}
