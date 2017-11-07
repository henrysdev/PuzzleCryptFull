import lombok.Data;

import java.util.stream.Stream;

@Data
public class Shard {

    private final byte[] payload;
    private final byte[] IV;
    private final byte[] HMAC;

    public byte[] toFragment () {
        return Stream.of(payload, IV, HMAC)
            .collect(Utils.byteCollector());
    }

}