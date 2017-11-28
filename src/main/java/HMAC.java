import lombok.SneakyThrows;

import java.util.Arrays;

public class HMAC {
    private byte[] value;

    /** Primary constructor. Generates new HMAC by the function
     * SHA256(AES_key + seq_ID)
     *
     * @param secretKey
     * @param seqID
     */
    @SneakyThrows
    public HMAC (String secretKey, int seqID) {
        this.value = CryptoUtils.hash(secretKey.concat(Integer.toString(seqID)));
    }

    /** Alternate constructor for creating an instance of a
     * pre-generated HMAC object in byte[] representation
     *
     * @param hmac
     */
    public HMAC (byte[] hmac) {
        this.value = hmac;
    }

    /** Alternate constructor for creating an instance of a
     * pre-generated HMAC object in String representation
     *
     * @param hmac
     */
    public HMAC (String hmac) {
        this.value = hmac.getBytes();
    }

    public byte[] getValue () {
        return value;
    }

    @Override
    public String toString () {
        return Arrays.toString(value);
    }
}
