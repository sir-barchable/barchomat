package sir.barchable.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream with support for reading single bits.
 *
 * @author Sir Barchable
 *         Date: 22/04/15
 */

public class BitInputStream extends InputStream {
    private InputStream in;
    private int bitField = -1;
    private int mask;

    public BitInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        bitField = -1;
        return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        bitField = -1;
        return in.read(b, off, len);
    }

    /**
     * Read one bit from the stream in little endian order.
     * <p>
     * Consecutive calls to this method will consume one bit at a time from the underlying stream. If any of the other
     * read methods are called all remaining bits up to the next byte boundary will be discarded.
     *
     * @return the next bit from the stream in little endian order
     */
    public boolean readBit() throws IOException {
        if (bitField == -1) {
            bitField = in.read();
            if (bitField == -1) {
                throw new EOFException();
            }
            mask = 1;
        }

        boolean bit = (bitField & mask) != 0;
        if ((mask <<= 1) == 0x100) {
            bitField = -1;
        }
        return bit;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
