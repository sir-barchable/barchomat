package sir.barchable.clash.protocol;

import sir.barchable.clash.protocol.Protocol.StructDefinition;
import sir.barchable.clash.protocol.Protocol.StructDefinition.FieldDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper for structures.
 *
 * @see MessageFactory
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class Message {
    private TypeFactory typeFactory;
    private String typeName;
    private StructDefinition definition;
    private Map<String, Object> fields;

    Message(TypeFactory typeFactory, String typeName) {
        this(typeFactory, typeName, null);
    }

    Message(TypeFactory typeFactory, String typeName, Map<String, Object> fields) {
        this.typeFactory = typeFactory;
        this.typeName = typeName;
        TypeFactory.Type type = typeFactory.resolveType(typeName);
        if (!type.isStruct()) {
            throw new TypeException("Not a struct type");
        }
        this.definition = type.getStructDefinition();
        this.fields = fields == null ?  new LinkedHashMap<>() : fields;
    }

    public StructDefinition getDefinition() {
        return definition;
    }

    /**
     * The pdu type, or {@link Pdu.Type#Unknown} if this message isn't a top level PDU message.
     *
     * @return the PDU type, or  {@link Pdu.Type#Unknown}
     */
    public Pdu.Type getType() {
        return Pdu.Type.valueOf(definition.getId());
    }

    public void set(String key, Object value) {
        fields.put(key, value);
    }

    public Object get(String key) {
        return fields.get(key);
    }

    public Map<String, Object> getFields(String key) {
        return (Map<String, Object>) fields.get(key);
    }

    public Message getMessage(String key) {
        FieldDefinition field = definition.getField(key);
        return new Message(typeFactory, field.getType(), (Map<String, Object>) fields.get(key));
    }

    public Boolean getBoolean(String key) {
        return (Boolean) fields.get(key);
    }

    public Byte getByte(String key) {
        return (Byte) fields.get(key);
    }

    public byte[] getBytes(String key) {
        return (byte[]) fields.get(key);
    }

    public Integer getInt(String key) {
        return (Integer) fields.get(key);
    }

    public Long getLong(String key) {
        return (Long) fields.get(key);
    }

    public String getString(String key) {
        return (String) fields.get(key);
    }

    public Message[] getArray(String key) {
        FieldDefinition field = definition.getField(key);
        TypeFactory.Type type = typeFactory.resolveType(field.getType());
        if (!type.isArray()) {
            throw new TypeException("Not an array type");
        }
        if (type.getStructDefinition() == null) {
            throw new TypeException("Not a struct array");
        }
        Object[] objects = (Object[]) fields.get(key);
        Message[] messages = new Message[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Map<String, Object> fields = (Map<String, Object>) objects[i];
            messages[i] = new Message(typeFactory, type.getName(), fields);
        }
        return messages;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public String getTypeName() {
        return typeName;
    }
}
