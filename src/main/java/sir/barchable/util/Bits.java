package sir.barchable.util;

/**
 * @author Sir Barchable
 *         Date: 20/04/15
 */
public class Bits {
    public static int swapEndian(int i) {
        return
            i << 24 |
            i << 8 & 0xff0000 |
            i >> 8 & 0xff00 |
            i >>> 24;
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
