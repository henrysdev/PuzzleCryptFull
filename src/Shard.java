

public class Shard {

    private byte[] dataPayload;
    private String HMAC;

    public Shard (byte[] payload) {
        dataPayload = payload;
    }

    public byte[] getDataPayload () {
        return dataPayload;
    }

    public String getHMAC () {
        return HMAC;
    }

    public byte[] getBytes () {
        return  dataPayload;
    }

}
