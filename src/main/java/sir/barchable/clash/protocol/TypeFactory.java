package sir.barchable.clash.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.Protocol.StructDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sir Barchable
 *         Date: 21/04/15
 */
public class TypeFactory {
    private static final Logger log = LoggerFactory.getLogger(TypeFactory.class);

    private static Pattern TYPE_PATTERN = Pattern.compile("(\\?)?(\\w+)(\\[(\\d+)?\\])?");

    public static final String ID_FIELD = "id";

    /**
     * A map from message struct name -> definition
     */
    private Map<String, StructDefinition> types = new LinkedHashMap<>();

    public enum Primitive {
        BOOLEAN, BYTE, INT, LONG, STRING, ZIP_STRING;
    }

    /**
     * Read the default protocol definition from the Protocol.json in this package.
     */
    public TypeFactory() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("Reading protocol definition from class path");
            try (InputStream in = Protocol.class.getResourceAsStream("Protocol.json")) {
                Protocol protocol = mapper.readValue(in, Protocol.class);
                init(protocol);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public TypeFactory(Protocol protocol) {
        init(protocol);
    }

    private void init(Protocol protocol) {
        // Populate the type map
        for (StructDefinition structDefinition : protocol.getMessages()) {
            String name = structDefinition.getName();
            types.put(name, structDefinition);
        }
    }

    /**
     * Find the message name for a given message ID.
     *
     * @param messageId the message ID to search for
     * @return the message name if defined
     */
    public Optional<String> getStructNameForId(int messageId) {
        if (messageId <= 0) {
            throw new IllegalArgumentException();
        }
        for (StructDefinition definition: types.values()) {
            if (definition.getId() != null && definition.getId() == messageId) {
                return Optional.ofNullable(definition.getName());
            }
        }
        return Optional.empty();
    }

    public StructDefinition getStructDefinitionForId(int id) {
        Optional<String> messageName = getStructNameForId(id);
        if (messageName.isPresent()) {
            return types.get(messageName.get());
        } else {
            return null;
        }
    }

    public Type parse(String typeDefinition) {
        Matcher matcher = TYPE_PATTERN.matcher(typeDefinition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Badly formed type name " + typeDefinition);
        }
        String optional = matcher.group(1);
        String name = matcher.group(2);
        String array = matcher.group(3);
        String arraySize = matcher.group(4);

        boolean isOptional = optional != null;
        StructDefinition struct = types.get(name);
        boolean isArray = array != null;
        int length = (arraySize == null) ? 0 : Integer.parseInt(arraySize);

        if (struct != null) {
            return new Type(isOptional, name, isArray, length, struct);
        } else {
            try {
                return new Type(isOptional, name, isArray, length, Primitive.valueOf(name));
            } catch (IllegalArgumentException e) {
                throw new TypeException("Unknown type " + typeDefinition);
            }
        }
    }

    /**
     * A big ball of mud containing everything the reader needs to know about a type.
     */
    public class Type {
        private boolean optional;
        private String name;
        private boolean array;
        private int length;
        private StructDefinition structDefinition;
        private Primitive primitiveType;

        public Type(boolean optional, String name, boolean array, int length, StructDefinition structDefinition) {
            this.optional = optional;
            this.name = name;
            this.array = array;
            this.length = length;
            this.structDefinition = structDefinition;
        }

        public Type(boolean optional, String name, boolean array, int length, Primitive primitiveType) {
            this.optional = optional;
            this.name = name;
            this.array = array;
            this.length = length;
            this.primitiveType = primitiveType;
        }

        public boolean isOptional() {
            return optional;
        }

        public boolean isPrimitive() {
            return primitiveType != null;
        }

        public String getName() {
            return name;
        }

        public boolean isArray() {
            return array;
        }

        public int getLength() {
            return length;
        }

        public StructDefinition getStructDefinition() {
            return structDefinition;
        }

        public Primitive getPrimitiveType() {
            return primitiveType;
        }
    }
}
