package sir.barchable.util;

import java.io.OutputStream;

/**
 * Somewhere to put stuff you don't want.
 *
 * @author Sir Barchable
 *         Date: 15/04/1
 */
public final class BitBucket extends OutputStream {
    public static final BitBucket NOWHERE = new BitBucket();

    /**
     * @see #NOWHERE
     */

    private BitBucket() {}

    @Override
    public void write(int b) { }

    @Override
    public void write(byte[] b, int off, int len) { }
}
