package sir.barchable.clash.protocol;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.ResourceException;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Read and write Protocol json.
 *
 * @author Sir Barchable
 *         Date: 6/04/15
 */
public class ProtocolTool {
    @Parameter(names = {"-d", "--definition-dir"}, required = true, description = "Directory to load the protocol definition from")
    private File resourceDir;

    @Parameter(names = {"-o", "--outfile"}, description = "Output file. Will write to stdout if not set")
    private File outFile;

    private static final Logger log = LoggerFactory.getLogger(ProtocolTool.class);

    public static void main(String[] args) {
        ProtocolTool tool = new ProtocolTool();
        JCommander commander = new JCommander(tool);
        try {
            commander.parse(args);
            tool.run();
        } catch (ParameterException e) {
            commander.usage();
        } catch (Exception e) {
            log.error("Oops: ", e);
        }
    }

    ProtocolTool() { }

    public ProtocolTool(File resourceDir) {
        this.resourceDir = resourceDir;
    }

    private void run() throws IOException {
        if (!resourceDir.exists() || !resourceDir.isDirectory()) {
            throw new FileNotFoundException(resourceDir.toString());
        } else {
            OutputStreamWriter out;
            if (outFile == null) {
                out = new OutputStreamWriter(System.out);
            } else {
                out = new OutputStreamWriter(new FileOutputStream(outFile), UTF_8);
            }
            try {
                write(read(), out);
            } finally {
                out.close();
            }
        }
    }

    /**
     * Read a collection of message definitions from a directory.
     *
     * @return a protocol definition
     */
    public Protocol read() throws IOException {
        if (!resourceDir.exists()) {
            throw new FileNotFoundException(resourceDir.toString());
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Protocol.StructDefinition> definitions = new ArrayList<>();

        log.info("Reading protocol definition from {}", resourceDir.getAbsolutePath());
        Files.walk(resourceDir.toPath())
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(path -> {
                try {
                    definitions.add(mapper.readValue(path.toFile(), Protocol.StructDefinition.class));
                } catch (IOException e) {
                    throw new ResourceException("Could not read definition file " + path.getFileName(), e);
                }
            });

        if (definitions.size() == 0) {
            throw new IOException("Protocol definition not found in " + resourceDir);
        }

        return new Protocol(definitions);
    }

    /**
     * Write a protocol definition out as json.
     *
     * @param protocol the definition
     * @param out where to write the definition
     */
    public void write(Protocol protocol, Writer out) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(out, protocol);
    }
}
