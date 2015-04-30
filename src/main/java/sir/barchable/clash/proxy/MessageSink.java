package sir.barchable.clash.proxy;

import java.util.Map;

/**
 * @author Sir Barchable
 *         Date: 18/04/15
 */
public interface MessageSink {
    void send(int id, Map<String, Object> message);
}
