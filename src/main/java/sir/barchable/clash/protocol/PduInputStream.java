package sir.barchable.clash.protocol;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sir Barchable
 */
public class PduInputStream extends DataInputStream {

    private Clash7Crypt cipher = new Clash7Crypt();

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public PduInputStream(InputStream in) {
        super(in);
    }

    public final int readUnsignedInt3() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        if ((ch1 | ch2 | ch3) < 0) {
            throw new EOFException();
        }
        return (ch1 << 24) + (ch2 << 8) + ch3;
    }

    public final byte[] readBytes(int len) throws IOException {
        byte[] b = new byte[len];
        readFully(b);
        return b;
    }

    public final Pdu readPdu() throws IOException {
        Pdu pdu = new Pdu();
        pdu.id = readUnsignedShort();
        int length = readUnsignedInt3();
        pdu.padding = readUnsignedShort();
        pdu.payload = cipher.encrypt(readBytes(length));
        return pdu;
    }

    public void setKey(byte[] nonce) {
        cipher.setKey(nonce);
    }
}
