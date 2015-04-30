package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.MessageReader;
import sir.barchable.clash.protocol.PduException;
import sir.barchable.clash.protocol.Pdu;

import java.io.IOException;
import java.util.Map;

/**
 * A filter that deserializes Pdus and hands them off to a {@link MessageTap} for analysis.
 *
 * @author Sir Barchable
 *         Date: 18/04/15
 */
public class MessageTapFilter implements PduFilter {
    private static final Logger log = LoggerFactory.getLogger(MessageTapFilter.class);

    private MessageReader messageReader;
    private MessageTap[] taps;

    public MessageTapFilter(MessageReader messageReader, MessageTap... taps) {
        this.messageReader = messageReader;
        this.taps = taps;
    }

    @Override
    public Pdu filter(Pdu pdu) throws IOException {
        try {
            Map<String, Object> message = messageReader.readMessage(pdu);
            if (message != null) {
                for (MessageTap tap : taps) {
                    tap.onMessage(pdu.getId(), message);
                }
            }
        } catch (PduException e) {
            log.warn(
                "Unable to deserialize message of type {}: {}",
                pdu.getId(),
                e.getMessage() == null ? e.toString() : e.getMessage()
            );
        }
        return pdu;
    }
}
