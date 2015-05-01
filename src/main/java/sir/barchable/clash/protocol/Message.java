package sir.barchable.clash.protocol;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class Message {
    private Protocol.MessageDefinition definition;
    private Map<String, Object> fields = new LinkedHashMap<>();

    public Message(Protocol.MessageDefinition definition) {
        this.definition = definition;
    }

    public void set(String key, Object value) {
        fields.put(key, value);
    }
}
