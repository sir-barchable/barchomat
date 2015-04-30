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
}
