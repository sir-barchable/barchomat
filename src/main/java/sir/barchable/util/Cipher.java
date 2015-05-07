package sir.barchable.util;

/**
 * Interface for ciphers.
 *
 * @author Sir Barchable
 *         Date: 7/05/15
 */
public interface Cipher {

    /**
     * Encrypt a byte[]. The input buffer is not modified by this operation.
     *
     * @param b the bytes to encrypt
     * @return the encrypted bytes
     */
    byte[] encrypt(byte[] b);

    /**
     * Set the key for encryption.
     */
    void setKey(byte[] key);
}
