

public class Shard {

    private String dataPayload;
    private String HMAC;

    public Shard (String payload) {
        dataPayload = payload;
    }

    public String getDataPayload () {
        return dataPayload;
    }

    public String getHMAC () {
        return HMAC;
    }

}
