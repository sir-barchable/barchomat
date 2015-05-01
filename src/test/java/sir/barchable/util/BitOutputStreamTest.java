package sir.barchable.util;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;

/**
 * @author Sir Barchable
 */
public class BitOutputStreamTest extends TestCase {

    /**
     * Test single bit write.
     */
    public void testWriteBit() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BitOutputStream bOut = new BitOutputStream(out);
        bOut.writeBit(true);
        byte[] bytes = out.toByteArray();
        // Nothing flushed yet
        Assert.assertEquals(0, bytes.length);
        bOut.close();
        bytes = out.toByteArray();
        Assert.assertEquals(1, bytes[0]);
    }

    /**
     * Test that writes flush at byte boundaries.
     */
    public void testWriteBits() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BitOutputStream bOut = new BitOutputStream(out);
        // lsb
        bOut.writeBit(true);
        bOut.writeBit(false);
        bOut.writeBit(true);
        bOut.writeBit(false);
        bOut.writeBit(true);
        bOut.writeBit(false);
        bOut.writeBit(true);
        // msb written... should have flushed byte
        bOut.writeBit(false);
        byte[] bytes = out.toByteArray();
        Assert.assertEquals(1, bytes.length);
        Assert.assertEquals(0b01010101, bytes[0]);
        // write one more
        bOut.writeBit(true);
        bOut.close();
        bytes = out.toByteArray();
        Assert.assertEquals(2, bytes.length);
        Assert.assertEquals(0b00000001, bytes[1]);
    }

    /**
     * Test that normal write operations flush the accumulated bits.
     */
    public void testWriteByte() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BitOutputStream bOut = new BitOutputStream(out);
        // lsb
        bOut.writeBit(true);
        byte[] bytes = out.toByteArray();
        // nothing yet
        Assert.assertEquals(0, bytes.length);
        // write one full byte
        bOut.write(2);
        bytes = out.toByteArray();
        // should have flushed the bit field and written one byte after it
        Assert.assertEquals(2, bytes.length);
        Assert.assertEquals(1, bytes[0]);
        Assert.assertEquals(2, bytes[1]);
    }
}
