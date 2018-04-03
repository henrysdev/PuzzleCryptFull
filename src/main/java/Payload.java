import lombok.SneakyThrows;

import java.util.Arrays;

public class Payload {
    private byte[] value;

    public Payload (byte[] d) {
        this.value = d;
    }

    public byte[] getValue () {
        return value;
    }

    public int size () {
        return value.length;
    }

    /** Encrypt payload locally
     *
     * @param aesCipher
     */
    @SneakyThrows
    public void encrypt (AESEncrypter aesCipher) {
        value = aesCipher.encrypt(value);
    }

    /** Decrypt payload locally
     *
     * @param aesCipher
     */
    @SneakyThrows
    public void decrypt (AESEncrypter aesCipher) {
        value = aesCipher.decrypt(value);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
}
