package sir.barchable.clash.protocol;

import java.io.*;

/**
 * Read Clash PDUs.
 *
 * @author Sir Barchable
 */
public class PduInputStream implements Closeable {
    private InputStream in;
    private Clash7Crypt cipher = new Clash7Crypt();

    /**
     * Creates a PDU input stream with a newly initialized stream cipher.
     * Call {@link #setKey(byte[])} after key exchange to reinitialize the stream cipher.
     *
     * @param in the stream to read from
     */
    public PduInputStream(InputStream in) {
        this.in = in;
    }

    private int readUInt3() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        if ((ch1 | ch2 | ch3) < 0) {
            throw new EOFException();
        }
        return (ch1 << 24) | (ch2 << 8) | ch3;
    }

    public final int readUInt2() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (ch1 << 8) | ch2;
    }

    private byte[] readBytes(int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        byte[] b = new byte[len];
        int n = 0;
        while (n < len) {
            int count = in.read(b, n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
        return b;
    }

    public final Pdu readPdu() throws IOException {
        Pdu pdu = new Pdu();
        pdu.id = readUInt2();
        int length = readUInt3();
        pdu.version = readUInt2();
        pdu.payload = cipher.encrypt(readBytes(length));
        return pdu;
    }

    public void setKey(byte[] nonce) {
        cipher.setKey(nonce);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
