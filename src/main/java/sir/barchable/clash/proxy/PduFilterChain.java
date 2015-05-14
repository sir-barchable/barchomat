package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.Pdu;

import java.io.IOException;

/**
 * @author Sir Barchable
 *         Date: 15/04/15
 */
public class PduFilterChain implements PduFilter {
    private static final Logger log = LoggerFactory.getLogger(PduFilterChain.class);

    private PduFilter[] chain;

    public PduFilterChain() {
        this.chain = new PduFilter[0];
    }

    public PduFilterChain(PduFilter... chain) {
        this.chain = chain;
    }

    @Override
    public Pdu filter(Pdu pdu) throws IOException {
        for (PduFilter filter : chain) {
            try {
                pdu = filter.filter(pdu);
                if (pdu == null) {
                    break;
                }
            } catch (RuntimeException e) {
                // Deal with bad filters
                log.warn("Unexpected exception from filter", e);
            }
        }
        return pdu;
    }

    public PduFilterChain addBefore(PduFilter... filters) {
        return new PduFilterChain(concat(filters, chain));
    }

    public PduFilterChain addAfter(PduFilter... filters) {
        return new PduFilterChain(concat(chain, filters));
    }

    private PduFilter[] concat(PduFilter[] a, PduFilter[] b) {
        PduFilter[] c = new PduFilter[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
