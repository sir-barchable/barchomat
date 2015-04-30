package sir.barchable.clash.proxy;

import sir.barchable.clash.protocol.Pdu;

import java.io.IOException;

/**
 * @author Sir Barchable
 *         Date: 15/04/15
 */
public class PduFilterChain implements PduFilter {
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
            pdu = filter.filter(pdu);
            if (pdu == null) {
                break;
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
