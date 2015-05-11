package sir.barchable.clash;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author Sir Barchable
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Parameter(names = {"-d", "--definition-dir"}, description = "Directory to load the protocol definition from")
    private File resourceDir;

    @Parameter(names = {"-l", "--logic"}, description = "Directory/file to load the game logic from")
    private File logicFile;

    @Parameter(names = {"-w", "--working-dir"}, description = "Directory to read streams from")
    private File workingDir = new File(".");

    public static void main(String[] args) throws IOException {
        Main main = new Main();
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

    private void run() {

    }
}
