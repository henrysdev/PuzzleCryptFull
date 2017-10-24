import java.io.ByteArrayOutputStream;

public class Shard {

    private byte[] payload;
    private byte[] HMAC;
    private byte[] IV;

    public Shard (byte[] encrPayload, byte[] iv, byte[] hmac) {
        payload = encrPayload;
        IV = iv;
        HMAC = hmac;
    }

    public byte[] getPayload () {
        return payload;
    }

    public byte[] getHMAC () {
        return HMAC;
    }

    public byte[] getIV () {
        return IV;
    }

    public byte[] toFragment () throws Exception {
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        fragmentStream.write( payload );
        fragmentStream.write( IV );
        fragmentStream.write( HMAC );
        byte[] fragment = fragmentStream.toByteArray();
        return fragment;
    }

}