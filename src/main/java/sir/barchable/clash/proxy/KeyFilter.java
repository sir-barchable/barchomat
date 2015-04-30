package sir.barchable.clash.proxy;

import sir.barchable.clash.protocol.Clash7Random;
import sir.barchable.clash.protocol.MessageReader;
import sir.barchable.clash.protocol.Pdu;

import java.util.Map;

/**
 * Manage key exchange.
 */
class KeyFilter implements PduFilter {
    private MessageReader reader = new MessageReader();
    private Clash7Random prng;

    private byte[] key;

    @Override
    public Pdu filter(Pdu pdu) {
        switch (pdu.getId()) {

            // Client generates the seed for the random number generator
            case 10101:
                Map<String, Object> loginMessage = reader.readMessage(pdu);
                prng = new Clash7Random((Integer) loginMessage.get("clientSeed"));
                break;

            // Server generates the nonce
            case 20000:
                if (prng == null) {
                    throw new IllegalStateException("No login");
                }
                Map<String, Object> keyMessage = reader.readMessage(pdu);
                byte[] nonce = (byte[]) keyMessage.get("serverRandom");
                // Generate the key
                key = prng.scramble(nonce);
                // Set the key in all streams
                break;

            default:
                throw new IllegalStateException("Pdu " + pdu.getId() + " before key exchange");
        }
        return pdu;
    }

    public byte[] getKey() {
        return key;
    }
}
