package sir.barchable.clash.proxy;

import sir.barchable.clash.protocol.Pdu;

import java.io.IOException;

/**
 * @author Sir Barchable
 *         Date: 15/04/15
 */
public interface PduFilter {
    Pdu filter(Pdu pdu) throws IOException;
}
