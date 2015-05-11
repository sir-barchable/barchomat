package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.*;

import java.util.Map;

/**
 * Manage key exchange.
 */
class KeyFilter implements PduFilter {
    private static final Logger log = LoggerFactory.getLogger(KeyFilter.class);

    private MessageFactory messageFactory;
    private Clash7Random prng;

    private byte[] key;

    public KeyFilter(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    @Override
    public Pdu filter(Pdu pdu) {
        switch (pdu.getId()) {

            // Client generates the seed for the random number generator
            case 10101:
                Message loginMessage = messageFactory.fromPdu(pdu);
                prng = new Clash7Random((Integer) loginMessage.get("clientSeed"));
                break;

            // Server generates the nonce
            case 20000:
                if (prng == null) {
                    throw new IllegalStateException("No login");
                }
                Message keyMessage = messageFactory.fromPdu(pdu);
                byte[] nonce = keyMessage.getBytes("serverRandom");
                // Generate the key
                key = prng.scramble(nonce);
                // Set the key in all streams
                break;

            // Login failure (update?)
            case 20103:
                Message loginFailedMessage = messageFactory.fromPdu(pdu);
                log.info("Login failed: {}", loginFailedMessage.get("failureReason"));
                break;

            default:
                log.warn("Pdu {} before key exchange", pdu.getId());
        }
        return pdu;
    }

    public byte[] getKey() {
        return key;
    }
}
