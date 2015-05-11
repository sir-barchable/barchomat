package sir.barchable.util;

/**
 * No-op {@link Cipher}. Simply clones the input byte[] during encryption.
 *
 * @author Sir Barchable
 *         Date: 7/05/15
 */
public class NoopCipher implements Cipher {
    public static final NoopCipher NOOP_CIPHER = new NoopCipher();

    /**
     * But see {@link #NOOP_CIPHER}
     */
    public NoopCipher() { }

    @Override
    public byte[] encrypt(byte[] b) {
        return b.clone();
    }

    @Override
    public void setKey(byte[] nonce) {
        // nothing to do
    }
}
