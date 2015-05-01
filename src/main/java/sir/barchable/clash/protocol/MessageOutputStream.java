package sir.barchable.clash.protocol;

import sir.barchable.util.BitOutputStream;
import sir.barchable.util.Bits;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.InflaterOutputStream;

/**
 * Output stream with support for writing Clash primitives.
 *
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class MessageOutputStream extends OutputStream {
    private BitOutputStream out;

    public MessageOutputStream(OutputStream out) {
        this.out = out instanceof BitOutputStream ? (BitOutputStream) out : new BitOutputStream(out);
    }

    public void writeBit(boolean bit) throws IOException {
        out.writeBit(bit);
    }

    public void writeInt(int v) throws IOException {
        out.write(v >>> 24);
        out.write(v >>> 16 & 0xff);
        out.write(v >>> 8 & 0xff);
        out.write(v & 0xff);
    }

    private byte buffer[] = new byte[8];

    public final void writeLong(long v) throws IOException {
        buffer[0] = (byte) (v >>> 56);
        buffer[1] = (byte) (v >>> 48);
        buffer[2] = (byte) (v >>> 40);
        buffer[3] = (byte) (v >>> 32);
        buffer[4] = (byte) (v >>> 24);
        buffer[5] = (byte) (v >>> 16);
        buffer[6] = (byte) (v >>> 8);
        buffer[7] = (byte) (v);
        out.write(buffer);
    }

    public void writeString(String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        // write length
        writeInt(bytes.length);
        // write utf-8 encoded string
        out.write(bytes);
    }

    public void writeZipString(String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InflaterOutputStream zipStream = new InflaterOutputStream(out);
        zipStream.write(bytes);
        zipStream.close();
        byte[] zipped = out.toByteArray();
        // write total length
        writeInt(zipped.length + 4);
        // write unzipped length in little endian order
        writeInt(Bits.swapEndian(bytes.length));
        // write zipped utf-8 encoded string
        write(zipped);
    }

    public void writeArray(int[] ints) throws IOException {
        // write length
        writeInt(ints.length);
        // write contents
        for (int i : ints) {
            writeInt(i);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }
}
