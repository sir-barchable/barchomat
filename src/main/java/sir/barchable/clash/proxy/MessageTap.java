package sir.barchable.clash.proxy;

import sir.barchable.clash.protocol.Pdu;

import java.util.Map;

/**
 * @author Sir Barchable
 *         Date: 18/04/15
 */
public interface MessageTap {
    void onMessage(Pdu.Type type, Map<String, Object> message);
}
