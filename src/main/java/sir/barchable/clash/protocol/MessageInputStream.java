package sir.barchable.clash.protocol;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.util.BitInputStream;
import sir.barchable.util.Bits;

import java.io.*;
import java.util.zip.InflaterInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Input stream with support for reading Clash primitives.
 *
 * @author Sir Barchable
 *         Date: 21/04/15
 */
public class MessageInputStream extends InputStream {
    /**
     * Maximum array length for sanity checks.
     */
    public static final int MAX_ARRAY_LENGTH = 1024 * 1024;

    private BitInputStream in;

    public MessageInputStream(InputStream in) {
        this.in = BitInputStream.toBitInputStream(in);
    }

    /**
     * Wrap or cast an InputStream to a MessageInputStream.
     */
    public static MessageInputStream toMessageInputStream(InputStream in) {
        return in instanceof MessageInputStream ? (MessageInputStream) in : new MessageInputStream(in);
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    public boolean readBit() throws IOException {
        return in.readBit();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public int readUnsignedByte() throws IOException {
        int ch = in.read();
        if (ch == -1) {
            throw new EOFException();
        }
        return ch;
    }

    public int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
    }

    private byte[] buffer = new byte[8];

    public long readLong() throws IOException {
        readArray(buffer);
        return
            ((long) buffer[0] << 56) +
            ((long) (buffer[1] & 255) << 48) +
            ((long) (buffer[2] & 255) << 40) +
            ((long) (buffer[3] & 255) << 32) +
            ((long) (buffer[4] & 255) << 24) +
            ((buffer[5] & 255) << 16) +
            ((buffer[6] & 255) << 8) +
            ((buffer[7] & 255));
    }

    public String readString() throws IOException {
        int length = readInt();
        checkLength(length);
        if (length == 0xffffffff) {
            // null sentinel
            return null;
        }
        return new String(readArray(new byte[length]), UTF_8);
    }

    public String readZipString() throws IOException {
        // Read data length
        int length = readInt();
        checkLength(length);
        if (length == 0xffffffff) {
            // null sentinel
            return null;
        }
        if (length == 0) {
            return "";
        }

        // Read unzipped Length
        int unzippedLength = Bits.swapEndian(readInt());
        checkLength(unzippedLength);

        // Read zipped data
        byte[] zipped = readArray(new byte[length - 4]);
        InflaterInputStream zipStream = new InflaterInputStream(new ByteArrayInputStream(zipped));
        ByteArrayOutputStream out = new ByteArrayOutputStream(unzippedLength);

        // Unzip it
        IOUtils.copy(zipStream, out);
        byte[] unzipped = out.toByteArray();

        // Decode the string
        return new String(unzipped, UTF_8);
    }

    public byte[] readArray(byte[] a) throws IOException {
        return readArray(a, 0, a.length);
    }

    public final byte[] readArray(byte a[], int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = in.read(a, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
        return a;
    }

    public int[] readArray(int[] a) throws IOException {
        for (int i = 0; i < a.length; i++) {
            a[i] = readInt();
        }
        return a;
    }

    public long[] readArray(long[] a) throws IOException {
        for (int i = 0; i < a.length; i++) {
            a[i] = readInt();
        }
        return a;
    }

    public String[] readArray(String[] a) throws IOException {
        for (int i = 0; i < a.length; i++) {
            a[i] = readString();
        }
        return a;
    }

    private void checkLength(int length) {
        if (length != 0xffffffff && length < 0 || length > MAX_ARRAY_LENGTH) {
            throw new PduException("String length out of bounds (" + length + ")");
        }
    }
}
