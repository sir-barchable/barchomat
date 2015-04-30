package sir.barchable.clash.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.ResourceException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Read and write Protocol json.
 *
 * @author Sir Barchable
 *         Date: 6/04/15
 */
public class ProtocolTool {
    private static final Logger log = LoggerFactory.getLogger(ProtocolTool.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
        } else {
            File dir = new File(args[0]);
            if (!dir.exists() || !dir.isDirectory()) {
                usage();
            } else {
                try {
                    write(read(dir), new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
                    System.exit(0);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
        System.exit(-1);
    }

    private static void usage() {
        System.err.println("usage: ProtocolTool path/to/protocol/dir");
    }

    /**
     * Read a collection of message definitions from a directory.
     *
     * @param dir the directory to read from
     * @return a protocol definition
     */
    public static Protocol read(File dir) throws IOException {
        if (!dir.exists()) {
            throw new FileNotFoundException(dir.toString());
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Protocol.MessageDefinition> definitions = new ArrayList<>();

        log.info("Reading protocol definition from {}", dir.getAbsolutePath());
        Files.walk(dir.toPath())
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(path -> {
                try {
                    definitions.add(mapper.readValue(path.toFile(), Protocol.MessageDefinition.class));
                } catch (IOException e) {
                    throw new ResourceException("Could not read definition file " + path.getFileName(), e);
                }
            });

        if (definitions.size() == 0) {
            throw new IOException("Protocol definition not found in " + dir);
        }

        return new Protocol(definitions);
    }

    /**
     * Write a protocol definition out as json.
     *
     * @param protocol the definition
     * @param out where to write the definition
     */
    public static void write(Protocol protocol, Writer out) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(out, protocol);
    }
}
