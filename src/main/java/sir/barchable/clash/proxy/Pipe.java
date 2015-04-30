package sir.barchable.clash.proxy;

import sir.barchable.clash.protocol.Pdu;
import sir.barchable.clash.protocol.PduInputStream;
import sir.barchable.clash.protocol.PduOutputStream;

import java.io.EOFException;
import java.io.IOException;

/**
 * A filtered pipe for clash streams.
 *
 * @author Sir Barchable
 *         Date: 15/04/15
 */
class Pipe {
    /**
     * Name for debugging
     */
    private final String name;

    /**
     * Close the sink on source EOF?
     */
    private boolean propagateEof = true;

    private PduInputStream source;
    private PduOutputStream sink;

    public Pipe(String name, PduInputStream source, PduOutputStream sink) {
        this.name = name;
        this.source = source;
        this.sink = sink;
    }

    /**
     * Pipe one PDU through the supplied filter.
     */
    public void filterThrough(PduFilter filter) throws IOException {
        // Read
        Pdu pdu;

        try {
            pdu = source.readPdu();
        } catch (EOFException eof) {
            if (propagateEof) {
                try {
                    sink.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            throw eof;
        }

        // Transform
        Pdu filteredPdu = filter.filter(pdu);

        if (filteredPdu != null) {
            // Write
            sink.writePdu(filteredPdu);
        };
    }

    public String getName() {
        return name;
    }

    public PduInputStream getSource() {
        return source;
    }

    public PduOutputStream getSink() {
        return sink;
    }

    public void setPropagateEof(boolean propagateEof) {
        this.propagateEof = propagateEof;
    }
}
