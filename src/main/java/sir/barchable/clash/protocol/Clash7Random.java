package sir.barchable.clash.protocol;

/**
 * Used to generate the cipher key from the seed provided by the server.
 * <p>
 * A Mersenne twister.
 */

public class Clash7Random {
    private static final int MATRIX_A = 0x9908B0df;
    private static final int UPPER_MASK = 0x80000000;
    private static final int LOWER_MASK = 0x7fffffff;
    private static final int TEMPERING_MASK_B = 0x9d2c5680;
    private static final int TEMPERING_MASK_C = 0xefc60000;

    private final int[] S = new int[624];
    private int ix = 0;

    /**
     * Seed the random number generator.
     *
     * @param seed the client seed, as sent to the server in the Login message
     */
    public Clash7Random(int seed) {
        for (int i = 0; i < S.length; i++) {
            S[i] = seed;
            seed = 1812433253 * ((seed ^ (seed >> 30)) + 1);
        }
    }

    /**
     * This is the clash 7 key generator. Generates the nonce used to form the RC4 key.
     *
     * @param serverRandom the random seed returned from the server
     * @return the nonce
     */
    public byte[] scramble(byte[] serverRandom) {
        byte[] result = new byte[serverRandom.length];
        // Mask is the 100th byte from the stream.
        byte mask = nextByte(100);
        // Xor the value provided by the server with prng stream and mask it with the above value
        // The mask is no doubt intended as an obfuscation step. Using & instead of ^ is an odd choice.
        for (int i = 0; i < serverRandom.length; i++) {
            result[i] = (byte) (serverRandom[i] ^ (nextByte() & mask));
        }
        return result;
    }

    /**
     * Get the low byte from the next int in the stream.
     */
    public byte nextByte() {
        return (byte) nextInt();
    }

    /**
     * Get the low order byte from the nth int in the stream.
     *
     * @param offset the distance to the byte to fetch
     */
    public byte nextByte(int offset) {
        skip(offset - 1);
        return nextByte();
    }

    /**
     * Skip forward <i>n</i> iterations.
     */
    public void skip(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < n; i++) {
            nextInt();
        }
    }

    public int nextInt() {
        if (ix == 0) {
            for (int i = 1, j = 0; i <= S.length; i++, j++) {
                int v4 = (S[i % S.length] & LOWER_MASK) + (S[j] & UPPER_MASK);
                int v6 = (v4 >> 1) ^ S[(i + 396) % S.length];
                if ((v4 & 1) == 1) {
                    v6 ^= MATRIX_A;
                }
                S[j] = v6;
            }
        }
        int val = S[ix];
        ix = (ix + 1) % S.length;
        val ^= (val >> 11) ^ ((val ^ (val >> 11)) << 7) & TEMPERING_MASK_B;
        val = (((val ^ (val << 15) & TEMPERING_MASK_C) >> 18) ^ val ^ (val << 15) & TEMPERING_MASK_C);
        return val;
    }
}
