import java.io.ByteArrayOutputStream;

public class Shard {

    private byte[] payload;
    private byte[] HMAC;

    public Shard (byte[] encrPayload, byte[] hmac) {
        payload = encrPayload;
        HMAC = hmac;
    }

    public byte[] getPayload () {
        return payload;
    }

    public byte[] getHMAC () {
        return HMAC;
    }

    public byte[] toFragment () throws Exception {
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        fragmentStream.write( payload );
        fragmentStream.write( HMAC );
        byte[] fragment = fragmentStream.toByteArray();
        return fragment;
    }

}