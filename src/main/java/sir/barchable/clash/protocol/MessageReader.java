package sir.barchable.clash.protocol;

import sir.barchable.clash.protocol.Protocol.MessageDefinition;
import sir.barchable.clash.protocol.Protocol.MessageDefinition.FieldDefinition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Sir Barchable
 *         Date: 6/04/15
 */
public class MessageReader {

    private TypeFactory typeParser;

    public MessageReader() {
        this(new TypeFactory());
    }

    public MessageReader(TypeFactory typeParser) {
        this.typeParser = typeParser;
    }

    /**
     * Read a message.
     *
     * @param pdu the PDU containing the message
     * @return a map of field names -> field values, or null if the message ID isn't recognized
     */
    public Map<String, Object> readMessage(Pdu pdu) {
        Optional<String> messageName = typeParser.getMessageNameForId(pdu.getId());
        if (messageName.isPresent()) {
            try {
                MessageInputStream in = new MessageInputStream(new ByteArrayInputStream(pdu.getPayload()));
                return (Map<String, Object>) readValue(messageName.get(), in);
            } catch (IOException e) {
                throw new PduException(e);
            }
        } else {
            return null;
        }
    }

    /**
     * Read a value from the input stream. Delegates to {@link #readValue(TypeFactory.Type definition, MessageInputStream)}
     * after parsing the type with the configured {@link TypeFactory}.
     */
    public Object readValue(String typeName, MessageInputStream in) throws IOException {
        if (typeName == null) {
            throw new NullPointerException("null definition");
        }

        return readValue(typeParser.parse(typeName), in);
    }

    /**
     * Read a value from the stream.
     *
     * @param definition the type of value to read
     * @param in where to read it from
     * @return the value, or null if the value was optional and not present in the stream
     */
    public Object readValue(TypeFactory.Type definition, MessageInputStream in) throws IOException {
        if (in == null) {
            throw new NullPointerException("null payload");
        }

        if (definition.isOptional() && !in.readBit()) {
            return null;
        }

        if (definition.isArray()) {
            return readArray(definition, in);
        } else if (definition.isPrimitive()) {
            return readPrimitive(definition, in);
        } else {
            return readStruct(definition, in);
        }
    }

    private Object readArray(TypeFactory.Type definition, MessageInputStream in) throws IOException {
        int length;
        if (definition.getLength() > 0) {
            // known length
            length = definition.getLength();
        } else {
            // length from stream
            length = in.readInt();
        }
        if (length < 0 || length > MessageInputStream.MAX_ARRAY_LENGTH) {
            throw new PduException("Array length out of bounds: " + length);
        }

        if (definition.isPrimitive()) {
            switch (definition.getPrimitiveType()) {
                case BYTE:
                    return in.readArray(new byte[length]);

                case INT:
                    return in.readArray(new int[length]);

                case LONG:
                    return in.readArray(new long[length]);

                case STRING:
                    return in.readArray(new String[length]);

                default:
                    throw new IllegalArgumentException("Don't know how to read arrays of type " + definition.getPrimitiveType());
            }
        } else {
            Object[] messages = new Object[length];
            for (int i = 0; i < length; i++) {
                try {
                    messages[i] = readValue(definition.getName(), in);
                } catch (PduException e) {
                    throw new PduException("Could not read element " + i + " of " + definition.getName() + "[]", e);
                }
            }
            return messages;
        }
    }

    private Object readPrimitive(TypeFactory.Type definition, MessageInputStream in) throws IOException {
        switch (definition.getPrimitiveType()) {
            case BOOLEAN:
                return in.readBit();

            case BYTE:
                return in.readUnsignedByte();

            case INT:
                return in.readInt();

            case LONG:
                return in.readLong();

            case STRING:
                return in.readString();

            case ZIP_STRING:
                return in.readZipString();

            default:
                throw new IllegalArgumentException("Don't know how to read " + definition.getName());
        }
    }

    private Object readStruct(TypeFactory.Type definition, MessageInputStream in) {
        Map<String, Object> fields = new LinkedHashMap<>();
        int fieldIndex = 0;
        try {

            // Read fields
            MessageDefinition struct = definition.getStruct();
            for (FieldDefinition field : struct.getFields()) {
                fieldIndex++;
                Object value = readValue(field.getType(), in);
                if (field.getName() != null) {
                    fields.put(field.getName(), value);
                }
            }

            // Extra fields from sub type?
            if (definition.getSubTypes() != null) {
                Integer id = (Integer) fields.get(TypeFactory.ID_FIELD);
                if (id == null) {
                    throw new PduException("id field missing from " + definition.getName());
                }
                MessageDefinition subTypeDefinition = definition.getSubTypes().get(id);
                if (subTypeDefinition != null) {
                    // Read extension to the struct
                    for (FieldDefinition field : subTypeDefinition.getFields()) {
                        fieldIndex++;
                        Object value = readValue(field.getType(), in);
                        if (field.getName() != null) {
                            fields.put(field.getName(), value);
                        }
                    }
                }
            }
        } catch (RuntimeException | IOException e) {
            throw new PduException("Could not read field " + fieldIndex + " of " + definition.getName(), e);
        }
        return fields;
    }
}
