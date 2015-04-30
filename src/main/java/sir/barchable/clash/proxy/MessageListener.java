package sir.barchable.clash.proxy;

import java.util.Map;

/**
 * @author Sir Barchable
 *         Date: 18/04/15
 */
public interface MessageListener {
    void clientMessage(int id, Map<String, Object> message);
    void serverMessage(int id, Map<String, Object> message);
}
