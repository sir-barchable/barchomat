package sir.barchable.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Write streams of bits to an output stream in little endian order.
 * <p>
 * Bit writes are committed to the underlying stream whenever a normal write operation occurs, or when 8 bit writes
 * accumulate. Commits always write up to the next byte boundary, leaving unset bits 0.
 *
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class BitOutputStream extends OutputStream {
    private OutputStream out;
    private int bitField;
    private int mask;

    public BitOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Commit any outstanding bit writes and then write a byte to the underlying output stream.
     */
    @Override
    public void write(int b) throws IOException {
        flushBits();
        out.write(b);
    }

    /**
     * Commit any outstanding bit writes and then write some bytes to the underlying output stream.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        flushBits();
        out.write(b, off, len);
    }

    /**
     * Write a bit to the underlying output stream in little endian order.
     * @param bit the bit to write
     */
    public void writeBit(boolean bit) throws IOException {
        if (bitField == -1) {
            bitField = 0;
            mask = 0b00000001;
        }
        if (bit) {
            bitField |= mask;
        }
        mask <<= 1;
        if (mask == 0b100000000) {
            flushBits();
        }
    }

    /**
     * Commit any outstanding bit writes and flush the underlying output stream.
     */
    @Override
    public void flush() throws IOException {
        flushBits();
        out.flush();
    }

    /**
     * Commit any outstanding bit writes. This always writes the bit field up to the next byte boundary, leaving any
     * unset bits 0.
     */
    public void flushBits() throws IOException {
        if (bitField != -1) {
            out.write(bitField);
            bitField = -1;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            flushBits();
        } finally {
            out.close();
        }
    }
}
