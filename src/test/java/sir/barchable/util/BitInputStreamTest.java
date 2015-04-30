package sir.barchable.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Sir Barchable
 *         Date: 30/04/15
 */
public class BitInputStreamTest {

    @Test
    public void testReadBit() throws Exception {
        InputStream src = new ByteArrayInputStream(new byte[] {0b00000001});
        BitInputStream in = new BitInputStream(src);
        Assert.assertEquals(true, in.readBit());
        Assert.assertEquals(false, in.readBit());
        Assert.assertEquals(-1, in.read());
    }

    @Test
    public void testFlushBits() throws Exception {
        InputStream src = new ByteArrayInputStream(new byte[] {1, 0, 1});
        BitInputStream in = new BitInputStream(src);
        Assert.assertEquals(true, in.readBit());
        Assert.assertEquals(false, in.readBit());
        Assert.assertEquals(0, in.read());
        Assert.assertEquals(true, in.readBit());
        Assert.assertEquals(false, in.readBit());
        Assert.assertEquals(-1, in.read());
    }

    @Test
    public void testRollMask() throws Exception {
        InputStream src = new ByteArrayInputStream(new byte[] {(byte) 0b10101010, 1});
        BitInputStream in = new BitInputStream(src);
        Assert.assertEquals(false, in.readBit());
        Assert.assertEquals(true, in.readBit());
        Assert.assertEquals(false, in.readBit());
        Assert.assertEquals(true, in.readBit());
        Assert.assertEquals(false, in.readBit());
        Assert.assertEquals(true, in.readBit());
        Assert.assertEquals(false, in.readBit());
        Assert.assertEquals(true, in.readBit());
        // Mask should roll over to 1 again
        Assert.assertEquals(true, in.readBit());
        Assert.assertEquals(false, in.readBit());
        Assert.assertEquals(-1, in.read());
    }

    @Test
    public void testRandom() throws IOException {
        int numBytes = 1024;

        byte[] bits = new byte[numBytes];
        ThreadLocalRandom.current().nextBytes(bits);
        BigInteger n = new BigInteger(bits);
        InputStream src = new ByteArrayInputStream(swapEndian(n.toByteArray()));
        BitInputStream in = new BitInputStream(src);

        int numBits = numBytes * 8;
        for (int i = 0; i < numBits; i++) {
            Assert.assertEquals(n.testBit(i), in.readBit());
        }
    }

    /**
     * In place endian swap for byte arrays
     *
     * @param bytes the bytes to swap the endianness of
     * @return the reordered input array
     */
    public static byte[] swapEndian(byte[] bytes) {
        for (int ia = 0; ia < bytes.length / 2; ia++) {
            int ib = bytes.length - 1 - ia;
            byte a = bytes[ia];
            bytes[ia] = bytes[ib];
            bytes[ib] = a;
        }
        return bytes;
    }
}
