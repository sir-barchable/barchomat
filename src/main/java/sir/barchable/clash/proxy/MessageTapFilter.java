package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.*;

import java.io.IOException;
import java.util.Map;

import static sir.barchable.clash.protocol.Pdu.Type.Unknown;

/**
 * A filter that deserializes Pdus and hands them off to a {@link MessageTap} for analysis.
 *
 * @author Sir Barchable
 *         Date: 18/04/15
 */
public class MessageTapFilter implements PduFilter {
    private static final Logger log = LoggerFactory.getLogger(MessageTapFilter.class);

    private MessageFactory messageFactory;
    private MessageTap[] taps;

    public MessageTapFilter(MessageFactory messageFactory, MessageTap... taps) {
        this.messageFactory = messageFactory;
        this.taps = taps;
    }

    @Override
    public Pdu filter(Pdu pdu) throws IOException {
        try {
            if (pdu.getType() == Unknown) {
                log.warn("Unknown PDU type {}", pdu.getId());
            } else {
                Message message = messageFactory.fromPdu(pdu);
                for (MessageTap tap : taps) {
                    tap.onMessage(message);
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
