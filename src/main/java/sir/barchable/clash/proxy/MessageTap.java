package sir.barchable.clash.proxy;

import java.util.Map;

/**
 * @author Sir Barchable
 *         Date: 18/04/15
 */
public interface MessageTap {
    void onMessage(int id, Map<String, Object> message);
}
