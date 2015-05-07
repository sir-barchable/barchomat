package sir.barchable.clash.protocol;

import sir.barchable.util.Cipher;
import sir.barchable.util.RC4;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Clash 7.1 packet cipher. RC4.
 *
 * @author Sir Barchable
 */
public class Clash7Crypt implements Cipher {
    // RC4 key prefix. Clearly a keyboard mash.
    private static final byte[] BASE_KEY = "fhsd6f86f67rt8fw78fw789we78r9789wer6re".getBytes(UTF_8);
    // The initial nonce is literally "nonce"
    private static final byte[] INITIAL_NONCE = "nonce".getBytes(UTF_8);

    private RC4 rc4;

    public Clash7Crypt() {
        setKey(INITIAL_NONCE);
    }

    @Override
    public byte[] encrypt(byte[] b) {
        return rc4.encrypt(b);
    }

    @Override
    public void setKey(byte[] nonce) {
        byte[] key = concat(BASE_KEY, nonce);
        rc4 = new RC4(key);
        rc4.skip(key.length);
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] key = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, key, a.length, b.length);
        return key;
    }
}
