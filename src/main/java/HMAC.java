import lombok.SneakyThrows;

import java.util.Arrays;

public class HMAC {
    private byte[] value;

    @SneakyThrows
    public HMAC (String secretKey, int seqID) {
        this.value = CryptoUtils.hash(secretKey.concat(Integer.toString(seqID)));
    }

    public HMAC (byte[] hmac) {
        this.value = hmac;
    }

    public HMAC (String hmac) {
        this.value = hmac.getBytes();
    }

    public byte[] getValue () {
        return value;
    }

    public void setValue (byte[] v) {
        this.value = v;
    }

    @Override
    public String toString () {
        return Arrays.toString(value);
    }
}
