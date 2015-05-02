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

    public void set(String key, Object value) {
        fields.put(key, value);
    }

    public Object get(String key) {
        return fields.get(key);
    }

    public Map<String, Object> getFields() {
        return fields;
    }
}
