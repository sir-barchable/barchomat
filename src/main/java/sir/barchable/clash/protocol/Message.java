package sir.barchable.clash.protocol;

import sir.barchable.clash.protocol.Protocol.StructDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class Message {
    private StructDefinition definition;
    private Map<String, Object> fields = new LinkedHashMap<>();

    public Message(StructDefinition definition) {
        this.definition = definition;
    }

    public Message(StructDefinition definition, Map<String, Object> fields) {
        this.definition = definition;
        this.fields = fields;
    }

    public StructDefinition getDefinition() {
        return definition;
    }

    public Pdu.Type getType() {
        return Pdu.Type.valueOf(definition.getId());
    }

    public void set(String key, Object value) {
        fields.put(key, value);
    }

    public Object get(String key) {
        return fields.get(key);
    }

    public Map<String, Object> getStruct(String key) {
        return (Map<String, Object>) fields.get(key);
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

    public Object[] getArray(String key) {
        return (Object[]) fields.get(key);
    }

    public Map<String, Object> getFields() {
        return fields;
    }
}
