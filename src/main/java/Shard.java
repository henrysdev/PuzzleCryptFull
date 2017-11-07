import lombok.Data;

import java.util.stream.Stream;

@Data
public class Shard {

    private final Byte[] payload;
    private final Byte[] IV;
    private final Byte[] HMAC;

    public byte[] toFragment(){
        return Stream.of(IV, payload, HMAC)
            .collect(Utils.byteCollector())
            .toByteArray();
    }
}