import lombok.Data;

import java.io.ByteArrayOutputStream;

@Data
public class Shard {

    private final byte[] payload;
    private final byte[] HMAC;
    private final byte[] IV;

    public byte[] toFragment() throws Exception {
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        fragmentStream.write(payload);
        fragmentStream.write(IV);
        fragmentStream.write(HMAC);
        return fragmentStream.toByteArray();
    }
}