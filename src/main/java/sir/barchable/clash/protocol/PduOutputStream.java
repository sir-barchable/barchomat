package sir.barchable.clash.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Sir Barchable
 *         Date: 6/04/15
 */
public class PduOutputStream extends DataOutputStream {
    private Clash7Crypt cipher = new Clash7Crypt();

    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream. The counter <code>written</code> is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later use.
     * @see java.io.FilterOutputStream#out
     */
    public PduOutputStream(OutputStream out) {
        super(out);
    }

    public void writeUnsignedInt3(int v) throws IOException {
        writeByte((v >>> 16) & 0xFF);
        writeByte((v >>> 8) & 0xFF);
        writeByte(v & 0xFF);
    }

    public void writePdu(Pdu pdu) throws IOException {
        writeShort(pdu.getId());
        writeUnsignedInt3(pdu.getPayload().length);
        writeShort(pdu.getPadding());
        write(cipher.encrypt(pdu.getPayload()));
    }

    public void setKey(byte[] nonce) {
        cipher.setKey(nonce);
    }
}
