package sir.barchable.clash.proxy;

import sir.barchable.clash.protocol.Message;

/**
 * @author Sir Barchable
 *         Date: 18/04/15
 */
public interface MessageTap {
    void onMessage(Message message);
}
