package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.Clash7Random;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.protocol.Pdu;

/**
 * Manage key exchange.
 */
class KeyTap implements MessageTap {
    private static final Logger log = LoggerFactory.getLogger(KeyTap.class);

    private Clash7Random prng;

    private byte[] key;

    public byte[] getKey() {
        return key;
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getType()) {

            // Client generates the seed for the random number generator
            case Login:
                prng = new Clash7Random(message.getInt("clientSeed"));
                break;

            // Server generates the nonce
            case Encryption:
                if (prng == null) {
                    throw new IllegalStateException("No login");
                }
                byte[] nonce = message.getBytes("serverRandom");
                // Generate the key
                key = prng.scramble(nonce);
                break;

            // Login failure (update?)
            case LoginFailed:
                log.info("Login failed: {}", message.get("failureReason"));
                break;

            default:
                log.warn("Pdu {} before key exchange", message.getType());
        }
    }
}
