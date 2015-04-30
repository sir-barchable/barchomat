package sir.barchable.clash.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.Protocol.MessageDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
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
     * A map from message type name -> definition
     */
    private Map<String, MessageDefinition> types = new LinkedHashMap<>();

    /**
     * A map from message type name -> sub type list
     */
    private Map<String, Map<Integer, MessageDefinition>> subTypes = new LinkedHashMap<>();

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

        // Populate the type maps
        for (MessageDefinition messageDefinition : protocol.getMessages()) {
            String name = messageDefinition.getName();
            String superType = messageDefinition.getExtends();
            if (superType != null) {
                addSubType(superType, messageDefinition);
            } else {
                types.put(name, messageDefinition);
            }
        }

        // Sanity checks
        for (String superType : subTypes.keySet()) {
            if (!types.containsKey(superType)) {
                // The super type isn't in the type map, probably a typo in the name
                throw new TypeException("Orphaned subtype of " + superType);
            }
            if (types.get(superType).getField(ID_FIELD) == null) {
                // No 'id' field, there's no way to resolve the subtype
                throw new TypeException("ID field required to resolve subtypes of " + superType);
            }
        }
    }

    private void addSubType(String type, MessageDefinition messageDefinition) {
        Map<Integer, MessageDefinition> subTypeDefinitions = subTypes.get(type);
        if (subTypeDefinitions == null) {
            subTypeDefinitions = new LinkedHashMap<>();
            subTypes.put(type, subTypeDefinitions);
        }
        int id = messageDefinition.getId();
        if (subTypeDefinitions.put(id, messageDefinition) != null) {
            throw new TypeException("Duplicated subtype ID " + id + " of " + type);
        }
    }

    /**
     * Find the message name for a given message ID.
     *
     * @param messageId the message ID to search for
     * @return the message name if defined
     */
    public Optional<String> getMessageNameForId(int messageId) {
        if (messageId <= 0) {
            throw new IllegalArgumentException();
        }
        for (MessageDefinition definition: types.values()) {
            if (definition.getId() != null && definition.getId() == messageId) {
                return Optional.ofNullable(definition.getName());
            }
        }
        return Optional.empty();
    }

    public Type parse(String definition) {
        Matcher matcher = TYPE_PATTERN.matcher(definition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Badly formed type name " + definition);
        }
        String optional = matcher.group(1);
        String name = matcher.group(2);
        String array = matcher.group(3);
        String arraySize = matcher.group(4);

        boolean isOptional = optional != null;
        MessageDefinition struct = types.get(name);
        boolean isArray = array != null;
        int length = (arraySize == null) ? 0 : Integer.parseInt(arraySize);

        if (struct != null) {
            return new Type(isOptional, name, isArray, length, struct, subTypes.get(name));
        } else {
            try {
                return new Type(isOptional, name, isArray, length, Primitive.valueOf(name));
            } catch (IllegalArgumentException e) {
                throw new TypeException("Unknown type " + definition);
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
        private MessageDefinition struct;
        private Map<Integer, MessageDefinition> subTypes;
        private Primitive primitiveType;

        public Type(boolean optional, String name, boolean array, int length, MessageDefinition struct, Map<Integer, MessageDefinition> subTypes) {
            this.optional = optional;
            this.name = name;
            this.array = array;
            this.length = length;
            this.struct = struct;
            this.subTypes = subTypes;
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

        public MessageDefinition getStruct() {
            return struct;
        }

        public Map<Integer, MessageDefinition> getSubTypes() {
            return subTypes;
        }

        public Primitive getPrimitiveType() {
            return primitiveType;
        }
    }
}
