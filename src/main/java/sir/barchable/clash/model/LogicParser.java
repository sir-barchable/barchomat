package sir.barchable.clash.model;

import au.com.bytecode.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.LZMAInputStream;
import sir.barchable.clash.ResourceException;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Sir Barchable
 *         Date: 21/04/15
 */
public final class LogicParser {
    private static final Logger log = LoggerFactory.getLogger(LogicParser.class);
    private static final String NAME_KEY = "Name";
    private static Pattern CSV_ENTRY_PATTERN = Pattern.compile("^.*logic.(\\w+)\\.csv$");

    public static void main(String[] args) throws IOException {
        Logic logic = loadLogic(new File("apk/assets/logic"));
        logic.dumpOids();
    }

    public static Logic loadLogic(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getName());
        }
        log.debug("Reading logic from {}", file.getCanonicalPath());
        if (file.isDirectory()) {
            return loadLogicFromDir(file);
        } else {
            return loadLogicFromApk(file);
        }
    }

    private static Logic loadLogicFromDir(File dir) throws IOException {
        Map<String, List<Logic.Data>> logic = new LinkedHashMap<>();

        Files.walk(dir.toPath())
            .forEach(entry -> {
                String path = entry.toString();
                Matcher matcher = CSV_ENTRY_PATTERN.matcher(path);
                if (matcher.matches()) {
                    log.debug("Loading {}", path);
                    try {
                        try (FileInputStream in = new FileInputStream(entry.toFile())) {
                            logic.put(matcher.group(1), loadLogicFile(in));
                        }
                    } catch (RuntimeException | IOException e) {
                        throw new ResourceException("Could not read logic file " + path, e);
                    }
                }
            });

        return new Logic(logic);
    }

    private static Logic loadLogicFromApk(File apk) throws IOException {
        Map<String, List<Logic.Data>> logic = new LinkedHashMap<>();

        JarFile jar = new JarFile(apk);
        jar.stream()
            .forEach(entry -> {
                String path = entry.getName();
                Matcher matcher = CSV_ENTRY_PATTERN.matcher(path);
                if (matcher.matches()) {
                    log.debug("Loading {}", path);
                    try (
                        InputStream in = newClashLzmaInputStream(jar.getInputStream(entry))
                    ) {
                        logic.put(matcher.group(1), loadLogicFile(in));
                    } catch (IOException e) {
                        throw new ResourceException(e);
                    }
                }
            });

        return new Logic(logic);
    }

    public static InputStream newClashLzmaInputStream(InputStream in) throws IOException {
        return new LZMAInputStream(new PatchStream(in));
    }

    /**
     * Patch the clash LZMA stream to insert four zeros after byte 8
     */
    static class PatchStream extends InputStream {
        InputStream in;
        int i;

        public PatchStream(InputStream in) {
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            int c;
            if (i >= 8 && i < 12) {
                c = 0;
            } else {
                c = in.read();
            }
            if (c != -1) {
                i++;
            }
            return c;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    static ArrayList<Logic.Data> loadLogicFile(InputStream in) throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(in));
        List<String[]> lines = reader.readAll();
        String[] header = lines.get(0);
        Logic.Data.Type[] types = parseTypes(lines.get(1));
        if (!NAME_KEY.equals(header[0]) || types[0] != Logic.Data.Type.STRING) {
            log.debug("Forcing Name column");
            header[0] = NAME_KEY;
            types[0] = Logic.Data.Type.STRING;
        }

        ArrayList<Logic.Data> logic = new ArrayList<>();

        Logic.Data data = null;
        for (int i = 2; i < lines.size(); i++) {
            String[] line = lines.get(i);
            if (!isNullOrEmpty(line[0])) {
                data = new Logic.Data(header);
                logic.add(data);
            } else if (data == null) {
                throw new ResourceException("Sub-header not found");
            }
            data.addLine(parseValues(types, line));
        }

        return logic;
    }

    private static Logic.Data.Type[] parseTypes(String[] typeNames) {
        Logic.Data.Type[] types = new Logic.Data.Type[typeNames.length];
        for (int i = 0; i < typeNames.length; i++) {
            types[i] = Logic.Data.Type.valueOf(typeNames[i].toUpperCase());
        }
        return types;
    }

    private static Object[] parseValues(Logic.Data.Type[] types, String[] line) {
        Object[] values = new Object[types.length];
        for (int i = 0; i < line.length; i++) {
            String s = line[i];
            if (!isNullOrEmpty(s)) {
                values[i] = types[i].parse(s);
            }
        }
        return values;
    }
}
